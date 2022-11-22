package app.vercel.shiftup.presentation.routes.auth

import app.vercel.shiftup.features.user.account.application.GetUserWithAutoRegisterUseCase
import app.vercel.shiftup.features.user.account.application.LoginOrRegisterException
import app.vercel.shiftup.features.user.account.domain.model.User
import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.account.domain.model.value.Name
import app.vercel.shiftup.features.user.domain.model.value.Email
import app.vercel.shiftup.presentation.firstManager
import app.vercel.shiftup.presentation.routes.auth.plugins.AUTH_OAUTH_GOOGLE_NAME
import app.vercel.shiftup.presentation.routes.auth.plugins.UserSession
import app.vercel.shiftup.presentation.routes.auth.plugins.configureAuthentication
import app.vercel.shiftup.presentation.routes.auth.plugins.configureSessions
import app.vercel.shiftup.presentation.topPageUrl
import com.github.michaelbull.result.getOrThrow
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.pipeline.*
import kotlinx.datetime.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import org.mpierce.ktor.csrf.noCsrfProtection
import kotlin.time.Duration

fun Application.authRouting(httpClient: HttpClient = app.vercel.shiftup.presentation.routes.auth.httpClient) {
    val config = environment.config
    configureSessions()
    configureAuthentication(httpClient)

    routing {
        noCsrfProtection {
            authenticate(AUTH_OAUTH_GOOGLE_NAME) {
                get<Login> {
                    // 自動的に認証ページに転送されます
                }

                get<Login.Verify> {
                    runCatching {
                        val user = getUserFromPrincipal()
                        call.apply {
                            // セッションIDはhttpOnlyで読み取れないので、ログイン判定用のCookieも保存する
                            response.cookies.append(
                                loggedInCookie(value = true, maxAge = UserSession.MAX_AGE)
                            )
                            sessions.set(
                                UserSession(
                                    userId = user.id,
                                    creationInstantISOString = Clock.System.now().toString()
                                )
                            )
                        }
                        call.respondRedirect(config.topPageUrl)
                    }.onFailure {
                        application.log.error(it.message)
                        when (it) {
                            is LoginOrRegisterException.InvalidUser -> call.respondRedirect(
                                config.topPageUrl + "/error/invalid-user",
                            )

                            else -> call.respondRedirect(
                                config.topPageUrl + "/error/authentication-error",
                            )
                        }
                    }
                }
            }

            get<Logout> {
                call.apply {
                    response.cookies.append(
                        loggedInCookie(value = false, maxAge = Duration.ZERO)
                    )
                    sessions.clear<UserSession>()
                }
                call.respondRedirect(config.topPageUrl)
            }

            authenticate {
                get<SessionAvailable> {
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.getUserFromPrincipal(): User {
    val principal: OAuthAccessTokenResponse.OAuth2 = checkNotNull(call.principal())
    val userInfo = httpClient.get("https://www.googleapis.com/oauth2/v2/userinfo") {
        headers {
            append(HttpHeaders.Authorization, "Bearer ${principal.accessToken}")
        }
    }.body<UserInfo>()

    val useCase by application.inject<GetUserWithAutoRegisterUseCase>()
    return userInfo.run {
        useCase(
            userId = UserId(id),
            name = Name(
                familyName = familyName,
                givenName = givenName,
            ),
            emailFactory = { Email(email) },
            firstManager = application.environment.config.firstManager,
        ).getOrThrow()
    }
}

@Suppress("unused")
@Serializable
@Resource("login")
class Login {
    @Serializable
    @Resource("verify")
    class Verify(val parent: Login)
}

@Serializable
@Resource("logout")
class Logout

@Serializable
@Resource("session-available")
class SessionAvailable

private val httpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
            }
        )
    }
}

@Serializable
private data class UserInfo(
    val id: String,
    val email: String,
    @SerialName("family_name") val familyName: String,
    @SerialName("given_name") val givenName: String,
)

private fun loggedInCookie(value: Boolean, maxAge: Duration) =
    Cookie(
        name = "logged_in",
        value = value.toString(),
        path = "/",
        maxAge = maxAge.inWholeSeconds.toInt(),
        extensions = mapOf("SameSite" to "lax")
    )

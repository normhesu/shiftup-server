package app.vercel.shiftup.presentation.routes.auth

import app.vercel.shiftup.features.user.account.application.GetAvailableUserWithAutoRegisterUseCase
import app.vercel.shiftup.features.user.account.application.LoginOrRegisterException
import app.vercel.shiftup.features.user.account.domain.model.AvailableUser
import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.account.domain.model.value.Name
import app.vercel.shiftup.features.user.domain.model.value.Email
import app.vercel.shiftup.features.user.domain.model.value.NeecStudentNumber
import app.vercel.shiftup.presentation.plugins.*
import app.vercel.shiftup.presentation.routes.inject
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
import org.mpierce.ktor.csrf.noCsrfProtection

fun Application.authRouting(
    httpClient: HttpClient = authenticationHttpClient,
) = routing {
    val config = this.application.environment.config
    noCsrfProtection {
        authenticate(AUTH_OAUTH_GOOGLE_NAME) {
            get<Login> {
                // 自動的に認証ページに転送されます
            }

            get<Login.Verify> {
                runCatching {
                    val user = getUserFromPrincipal(httpClient)
                    call.sessions.set(
                        UserSession(
                            userId = user.id,
                            creationInstantISOString = Clock.System.now().toString()
                        )
                    )
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
            call.sessions.clear<UserSession>()
            call.respondRedirect(config.topPageUrl)
        }

        authenticate {
            get<SessionAvailable> {
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.getUserFromPrincipal(httpClient: HttpClient): AvailableUser {
    val principal: OAuthAccessTokenResponse.OAuth2 = checkNotNull(call.principal())
    val userInfo = httpClient.get("https://www.googleapis.com/oauth2/v2/userinfo") {
        headers {
            append(HttpHeaders.Authorization, "Bearer ${principal.accessToken}")
        }
    }.body<UserInfo>()

    val useCase: GetAvailableUserWithAutoRegisterUseCase by inject()
    return userInfo.run {
        useCase(
            userId = UserId(id),
            name = Name(formattedName),
            emailFactory = { Email(email) },
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

@Serializable
private data class UserInfo(
    val id: String,
    val email: String,
    @SerialName("family_name") private val familyName: String,
    @SerialName("given_name") private val givenName: String,
) {
    val formattedName = runCatching { NeecStudentNumber(familyName) }.fold(
        onSuccess = { givenName.replace(" ", "") },
        onFailure = { familyName },
    )
}

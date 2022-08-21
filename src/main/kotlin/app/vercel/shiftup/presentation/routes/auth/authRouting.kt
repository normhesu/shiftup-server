package app.vercel.shiftup.presentation.routes.auth

import app.vercel.shiftup.features.user.account.application.GetUserWithAutoRegisterUseCase
import app.vercel.shiftup.features.user.account.application.LoginOrRegisterException
import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.account.domain.model.value.Name
import app.vercel.shiftup.features.user.domain.model.value.NeecEmail
import app.vercel.shiftup.presentation.firstManager
import app.vercel.shiftup.presentation.routes.auth.plugins.AUTH_OAUTH_GOOGLE_NAME
import app.vercel.shiftup.presentation.routes.auth.plugins.UserSession
import app.vercel.shiftup.presentation.routes.auth.plugins.configureAuthentication
import app.vercel.shiftup.presentation.routes.auth.plugins.configureSessions
import app.vercel.shiftup.presentation.topPageUrl
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.github.michaelbull.result.runCatching
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
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import org.mpierce.ktor.csrf.noCsrfProtection

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
                    val principal: OAuthAccessTokenResponse.OAuth2? = call.principal()
                    val userInfo: UserInfo = principal?.getUserInfo() ?: run {
                        call.respondRedirect(config.topPageUrl)
                        return@get
                    }

                    this@authRouting.getUserWithAutoRegister(
                        userInfo,
                    ).getOrElse {
                        this@authRouting.log.error(it.message)
                        call.respondRedirect(LoginFailure.PATH)
                        return@get
                    }.onSuccess { user ->
                        call.sessions.set(UserSession(user.id))
                        call.respondRedirect(config.topPageUrl)
                    }.onFailure {
                        when (it) {
                            is LoginOrRegisterException.InvalidUser -> {
                                call.respondRedirect(InvalidUser.PATH)
                            }
                            is LoginOrRegisterException.Other -> {
                                this@authRouting.log.error(it.message)
                                call.respondRedirect(LoginFailure.PATH)
                            }
                        }
                    }
                }
            }

            get<InvalidUser> {
                call.respondText("アカウント登録が許可されていません")
            }

            get<LoginFailure> {
                call.respondText("ログインに失敗しました")
            }

            get<Logout> {
                call.sessions.clear<UserSession>()
                call.respondRedirect(config.topPageUrl)
            }
        }
    }
}

private suspend fun Application.getUserWithAutoRegister(
    userInfo: UserInfo,
) = runCatching {
    val getUserWithAutoRegisterUseCase: GetUserWithAutoRegisterUseCase by inject()
    val firstManager = environment.config.firstManager
    coroutineScope {
        getUserWithAutoRegisterUseCase(
            userId = UserId(userInfo.id),
            name = Name(
                familyName = userInfo.familyName,
                givenName = userInfo.givenName,
            ),
            emailFactory = { NeecEmail(userInfo.email) },
            firstManager = firstManager,
        )
    }
}

private suspend fun OAuthAccessTokenResponse.OAuth2.getUserInfo(): UserInfo {
    return httpClient.get("https://www.googleapis.com/oauth2/v2/userinfo") {
        headers {
            append(HttpHeaders.Authorization, "Bearer $accessToken")
        }
    }.body()
}

@Serializable
@Resource("/login")
class Login {
    @Serializable
    @Resource("verify")
    class Verify(
        @Suppress("unused")
        val parent: Login = Login()
    )
}

@Serializable
@Resource(InvalidUser.PATH)
object InvalidUser {
    const val PATH = "/invalid-user"
}

@Serializable
@Resource(LoginFailure.PATH)
object LoginFailure {
    const val PATH = "/login-failure"
}

@Serializable
@Resource("/logout")
object Logout

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

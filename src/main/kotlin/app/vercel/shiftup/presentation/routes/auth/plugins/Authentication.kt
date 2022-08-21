package app.vercel.shiftup.presentation.routes.auth.plugins

import app.vercel.shiftup.presentation.googleClientId
import app.vercel.shiftup.presentation.googleClientSecret
import app.vercel.shiftup.presentation.serverRootUrl
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*

const val AUTH_OAUTH_GOOGLE_NAME = "auth-oauth-google"

fun Application.configureAuthentication(httpClient: HttpClient) {
    val config = environment.config
    val productMode = !developmentMode
    install(Authentication) {
        oauth(AUTH_OAUTH_GOOGLE_NAME) {
            urlProvider = {
                val path = "login/verify"
                if (productMode) {
                    "${config.serverRootUrl}/$path"
                } else {
                    "http://localhost:${config.port}/$path"
                }
            }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "google",
                    authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
                    accessTokenUrl = "https://accounts.google.com/o/oauth2/token",
                    requestMethod = HttpMethod.Post,
                    clientId = config.googleClientId,
                    clientSecret = config.googleClientSecret,
                    defaultScopes = listOf(
                        "https://www.googleapis.com/auth/userinfo.profile",
                        "https://www.googleapis.com/auth/userinfo.email",
                        "openid",
                    ),
                )
            }
            client = httpClient
        }

        session<UserSession> {
            validate { it }
            challenge {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
}

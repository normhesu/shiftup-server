package app.vercel.shiftup.presentation.routes.auth.plugins

import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.presentation.sessionSignKey
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import kotlinx.serialization.Serializable
import java.io.File
import kotlin.time.Duration.Companion.days

const val USER_SESSION_NAME = "user_session"

fun Application.configureSessions() {
    val config = environment.config
    val productMode = !developmentMode
    install(Sessions) {
        cookie<UserSession>(
            USER_SESSION_NAME,
            directorySessionStorage(
                rootDir = File("build/.sessions"),
                cached = true,
            )
        ) {
            cookie.apply {
                path = "/"
                maxAge = 30.days
                secure = productMode
                httpOnly = true
            }
            transform(
                SessionTransportTransformerMessageAuthentication(
                    hex(config.sessionSignKey)
                ),
            )
        }
    }
}

@Serializable
data class UserSession(val userId: UserId) : Principal

val CurrentSession.userId get() = get<UserSession>()?.userId

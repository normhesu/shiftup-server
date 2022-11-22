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

fun Application.configureSessions() {
    val config = environment.config
    install(Sessions) {
        cookie<UserSession>(
            "user_session",
            directorySessionStorage(
                rootDir = File("build/.sessions"),
                cached = true,
            )
        ) {
            cookie.apply {
                path = "/"
                httpOnly = true
                maxAge = UserSession.MAX_AGE
                extensions["SameSite"] = "lax"
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
data class UserSession(
    val userId: UserId,
    val creationInstantISOString: String,
) : Principal {
    companion object {
        val MAX_AGE = 7.days
    }
}

val CurrentSession.userId get() = get<UserSession>()?.userId

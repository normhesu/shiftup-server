package app.vercel.shiftup.presentation.routes.auth.plugins

import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.presentation.sessionSignKey
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import io.ktor.util.date.*
import kotlinx.serialization.Serializable
import java.io.File
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

fun Application.configureSessions() {
    val environment = environment
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
                secure = environment.developmentMode.not()
                maxAge = UserSession.MAX_AGE
                extensions["SameSite"] = "lax"
            }
            transform(
                SessionTransportTransformerMessageAuthentication(
                    hex(environment.config.sessionSignKey)
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

val CurrentSession.userId get() = get<UserSession>().let(::checkNotNull).userId

/**
 * Cookieにsecureを設定する際のHTTPSチェックを無視するために使用します
 */
private inline fun <reified S : Any> SessionsConfig.cookie(
    name: String,
    storage: SessionStorage,
    block: CookieIdSessionBuilder<S>.() -> Unit
) {
    val sessionType = S::class
    val builder = CookieIdSessionBuilder<S>(typeOf<S>()).apply(block)
    val transport = SessionTransportCookie(name, builder.cookie, builder.transformers)
    val tracker = SessionTrackerById(sessionType, builder.serializer, storage, builder.sessionIdProvider)
    val provider = SessionProvider(name, sessionType, transport, tracker)
    register(provider)
}

private class CookieIdSessionBuilder<S : Any>(
    typeInfo: KType,
) {
    val sessionIdProvider: () -> String = { generateSessionId() }
    val serializer: SessionSerializer<S> = defaultSessionSerializer(typeInfo)
    val cookie: CookieConfiguration = CookieConfiguration()

    private val _transformers = mutableListOf<SessionTransportTransformer>()
    val transformers: List<SessionTransportTransformer> get() = _transformers

    fun transform(transformer: SessionTransportTransformer) {
        _transformers.add(transformer)
    }
}

private class SessionTransportCookie(
    private val name: String,
    private val configuration: CookieConfiguration,
    private val transformers: List<SessionTransportTransformer>
) : SessionTransport {
    override fun receive(call: ApplicationCall): String? {
        return transformers.transformRead(call.request.cookies[name, configuration.encoding])
    }

    override fun send(call: ApplicationCall, value: String) {
        val now = GMTDate()
        val maxAge = configuration.maxAgeInSeconds

        @Suppress("MagicNumber")
        val expires = when (maxAge) {
            0L -> null
            else -> now + maxAge * 1000L
        }

        val cookie = Cookie(
            name,
            transformers.transformWrite(value),
            configuration.encoding,
            maxAge.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
            expires,
            configuration.domain,
            configuration.path,
            configuration.secure,
            configuration.httpOnly,
            configuration.extensions
        )
        call.response.apply {
            // セッションIDはhttpOnlyで読み取れないので、ログイン判定用のCookieも保存する
            cookies.append(
                loggedInCookie(value = true, maxAge = UserSession.MAX_AGE)
            )
            // cookies.appendを使用するとsecureを設定する際に失敗するため、直接ヘッダーに追加する
            appendCookieDirectly(cookie)
        }
    }

    override fun clear(call: ApplicationCall) {
        call.response.apply {
            cookies.append(
                loggedInCookie(value = false, maxAge = Duration.ZERO)
            )
            appendCookieDirectly(clearCookie())
        }
    }

    private fun ApplicationResponse.appendCookieDirectly(cookie: Cookie) = headers.append(
        "Set-Cookie",
        renderSetCookieHeader(cookie)
    )

    private fun clearCookie(): Cookie = Cookie(
        name,
        "",
        configuration.encoding,
        maxAge = 0,
        domain = configuration.domain,
        path = configuration.path,
        secure = configuration.secure,
        httpOnly = configuration.httpOnly,
        extensions = configuration.extensions,
        expires = GMTDate.START
    )

    override fun toString(): String {
        return "SessionTransportCookie: $name"
    }
}

private fun loggedInCookie(value: Boolean, maxAge: Duration) =
    Cookie(
        name = "logged_in",
        value = value.toString(),
        path = "/",
        maxAge = maxAge.inWholeSeconds.toInt(),
        extensions = mapOf("SameSite" to "lax"),
    )

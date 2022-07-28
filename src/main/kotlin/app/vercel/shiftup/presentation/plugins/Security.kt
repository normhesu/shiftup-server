package app.vercel.shiftup.presentation.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import org.mpierce.ktor.csrf.CsrfProtection
import org.mpierce.ktor.csrf.OriginMatchesKnownHost

private const val SCHEME = "https"
private const val HOST = "shiftup.vercel.app"

fun Application.configureSecurity() {
    val productMode = environment.developmentMode.not()
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        if (productMode) {
            allowHost(host = HOST, schemes = listOf(SCHEME))
        } else {
            anyHost()
        }
    }

    if (productMode) install(CsrfProtection) {
        applyToAllRoutes()
        validate(OriginMatchesKnownHost(SCHEME, HOST))
    }
}

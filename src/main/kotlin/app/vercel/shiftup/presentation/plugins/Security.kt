package app.vercel.shiftup.presentation.plugins

import app.vercel.shiftup.presentation.allowAllHosts
import app.vercel.shiftup.presentation.topPageUrl
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import org.mpierce.ktor.csrf.CsrfProtection
import org.mpierce.ktor.csrf.OriginMatchesKnownHost

fun Application.configureSecurity() {
    val allowAllHosts = environment.config.allowAllHosts
    val (scheme, host) = environment.config.topPageUrl
        .dropLastWhile { it == '/' }
        .split("://")

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        if (allowAllHosts) {
            anyHost()
        } else {
            allowHost(host = host, schemes = listOf(scheme))
        }
    }

    install(CsrfProtection) {
        if (!allowAllHosts) applyToAllRoutes()
        validate(OriginMatchesKnownHost(scheme, host))
    }
}

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
        HttpMethod.DefaultMethods.forEach(::allowMethod)
        allowHeader(HttpHeaders.Accept)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Cookie)
        allowHeader(HttpHeaders.Origin)
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

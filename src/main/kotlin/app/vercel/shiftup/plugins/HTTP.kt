package app.vercel.shiftup.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import org.mpierce.ktor.csrf.CsrfProtection
import org.mpierce.ktor.csrf.OriginMatchesKnownHost

fun Application.configureSecurity() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader("MyCustomHeader")
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }

    if (environment.developmentMode.not()) install(CsrfProtection) {
        applyToAllRoutes()
        validate(OriginMatchesKnownHost("https", "shiftup.vercel.app"))
    }
}

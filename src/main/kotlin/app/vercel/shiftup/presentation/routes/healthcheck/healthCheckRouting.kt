package app.vercel.shiftup.presentation.routes.healthcheck

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.mpierce.ktor.csrf.noCsrfProtection

fun Application.healthCheckRouting() {
    routing {
        noCsrfProtection {
            get("/health-check") {
                call.respondText("OK")
            }
        }
    }
}

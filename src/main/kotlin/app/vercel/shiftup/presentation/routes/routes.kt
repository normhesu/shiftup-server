package app.vercel.shiftup.presentation.routes

import app.vercel.shiftup.presentation.routes.healthcheck.healthCheckRouting
import io.ktor.server.application.*

fun Application.routes() {
    healthCheckRouting()
}

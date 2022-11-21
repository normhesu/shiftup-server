package app.vercel.shiftup.presentation.routes

import app.vercel.shiftup.presentation.routes.attendance.surveys.attendanceSurveysRouting
import app.vercel.shiftup.presentation.routes.auth.authRouting
import app.vercel.shiftup.presentation.routes.healthcheck.healthCheckRouting
import app.vercel.shiftup.presentation.routes.invites.invitesRouting
import app.vercel.shiftup.presentation.routes.users.usersRouting
import io.ktor.server.application.*

fun Application.routes() {
    healthCheckRouting()
    authRouting()
    invitesRouting()
    attendanceSurveysRouting()
    usersRouting()
}

package app.vercel.shiftup.presentation.routes.attendance.requests

import app.vercel.shiftup.presentation.routes.attendance.requests.date.attendanceRequestsDateRouting
import app.vercel.shiftup.presentation.routes.attendance.requests.me.attendanceRequestsMeRouting
import io.ktor.server.application.*

fun Application.attendanceRequestsRouting() {
    attendanceRequestsDateRouting()
    attendanceRequestsMeRouting()
}

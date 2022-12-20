package app.vercel.shiftup.presentation.routes.attendance.requests.date

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.request.application.ApplyAttendanceRequestToOpenCampusDateUseCase
import app.vercel.shiftup.features.attendance.request.application.ForcedChangeAttendanceRequestStateUseCase
import app.vercel.shiftup.features.attendance.request.domain.model.value.AttendanceRequestState
import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.domain.model.value.Role
import app.vercel.shiftup.presentation.routes.attendance.Attendance
import app.vercel.shiftup.presentation.routes.auth.plugins.routingWithRole
import app.vercel.shiftup.presentation.routes.auth.plugins.userId
import app.vercel.shiftup.presentation.routes.inject
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable

fun Application.attendanceRequestsDateRouting() = routingWithRole(Role.Manager) {
    put<Requests.Date> {
        val useCase: ApplyAttendanceRequestToOpenCampusDateUseCase by inject()
        useCase(
            openCampusDate = it.openCampusDate,
            userIds = call.receive(),
        )
        call.respond(HttpStatusCode.NoContent)
    }
    put<Requests.Date.Id.State> {
        val useCase: ForcedChangeAttendanceRequestStateUseCase by inject()
        useCase(
            userId = it.parent.id,
            openCampusDate = it.parent.parent.openCampusDate,
            state = AttendanceRequestState(
                enumValueOf(call.receiveText()),
            ),
            operatorId = call.sessions.userId,
        )
        call.respond(HttpStatusCode.NoContent)
    }
}

@Suppress("unused")
@Serializable
@Resource("requests")
class Requests(val parent: Attendance) {
    @Serializable
    @Resource("{openCampusDate}")
    class Date(val parent: Requests, val openCampusDate: OpenCampusDate) {
        @Serializable
        @Resource("{id}")
        class Id(val parent: Date, val id: UserId) {
            @Serializable
            @Resource("state")
            class State(val parent: Id)
        }
    }
}

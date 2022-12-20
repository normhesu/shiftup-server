package app.vercel.shiftup.presentation.routes.attendance.requests.me

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.request.application.GetRespondableAttendanceRequestUseCase
import app.vercel.shiftup.features.attendance.request.application.RespondAttendanceRequestUseCase
import app.vercel.shiftup.features.attendance.request.domain.model.value.AttendanceRequestState
import app.vercel.shiftup.features.user.account.domain.model.CastId
import app.vercel.shiftup.features.user.domain.model.value.Role
import app.vercel.shiftup.presentation.routes.auth.plugins.routingWithRole
import app.vercel.shiftup.presentation.routes.auth.plugins.userId
import app.vercel.shiftup.presentation.routes.inject
import app.vercel.shiftup.presentation.routes.users.Users
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable
import org.mpierce.ktor.csrf.noCsrfProtection

fun Application.attendanceRequestsMeRouting() = routingWithRole(Role.Cast) {
    noCsrfProtection {
        get<Requests> {
            @Serializable
            data class ResponseItem(
                val openCampusDate: OpenCampusDate,
                val castId: CastId,
            )

            val useCase: GetRespondableAttendanceRequestUseCase by inject()
            val response = useCase(call.sessions.userId).map {
                ResponseItem(
                    openCampusDate = it.openCampusDate,
                    castId = it.castId,
                )
            }
            call.respond(response)
        }
    }
    post<Requests.Date.State> {
        val useCase: RespondAttendanceRequestUseCase by inject()
        useCase(
            userId = call.sessions.userId,
            openCampusDate = it.parent.openCampusDate,
            state = AttendanceRequestState.NonBlank(
                enumValueOf(call.receiveText())
            ),
        ).onSuccess {
            call.respond(HttpStatusCode.OK)
        }.onFailure {
            call.response.headers.append(HttpHeaders.Allow, "")
            call.respond(HttpStatusCode.MethodNotAllowed)
        }
    }
}

@Suppress("unused")
@Serializable
@Resource("requests")
class Requests(val parent: Users.Me.Attendance) {
    @Serializable
    @Resource("{openCampusDate}")
    class Date(val parent: Requests, val openCampusDate: OpenCampusDate) {
        @Serializable
        @Resource("state")
        class State(val parent: Date)
    }
}
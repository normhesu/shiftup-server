package app.vercel.shiftup.presentation.routes.attendance.requests.date

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.request.application.ApplyAttendanceRequestToOpenCampusDateUseCase
import app.vercel.shiftup.features.attendance.request.application.ForcedChangeAttendanceRequestStateUseCase
import app.vercel.shiftup.features.attendance.request.application.ForcedChangeAttendanceRequestStateUseCaseException
import app.vercel.shiftup.features.attendance.request.application.GetCastsByAttendanceRequestUseCase
import app.vercel.shiftup.features.attendance.request.domain.model.value.AttendanceRequestState
import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.account.domain.model.value.Name
import app.vercel.shiftup.features.user.domain.model.value.*
import app.vercel.shiftup.features.user.invite.domain.model.value.Position
import app.vercel.shiftup.presentation.routes.attendance.Attendance
import app.vercel.shiftup.presentation.routes.auth.plugins.routingWithRole
import app.vercel.shiftup.presentation.routes.auth.plugins.userId
import app.vercel.shiftup.presentation.routes.inject
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable
import org.mpierce.ktor.csrf.noCsrfProtection

fun Application.attendanceRequestsDateRouting() = routingWithRole(Role.Manager) {
    noCsrfProtection {
        get<Requests.Date.Casts> {
            @Serializable
            data class ResponseItem(
                val id: UserId,
                val name: Name,
                val studentNumber: StudentNumber,
                val email: Email,
                val department: Department,
                val position: Position,
            )

            val useCase: GetCastsByAttendanceRequestUseCase by inject()
            val response = useCase(
                openCampusDate = it.parent.openCampusDate,
                state = it.state,
            ).map { cast ->
                val user = cast.value
                ResponseItem(
                    id = user.id,
                    name = user.name,
                    email = user.email,
                    department = user.department,
                    studentNumber = user.studentNumber,
                    position = user.position,
                )
            }

            call.respond(response)
        }
    }
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
        ).onSuccess {
            call.respond(HttpStatusCode.NoContent)
        }.onFailure { e ->
            when (e) {
                is ForcedChangeAttendanceRequestStateUseCaseException.NotFoundRequest -> {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
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

        @Serializable
        @Resource("casts")
        class Casts(val parent: Date, val state: AttendanceRequestState)
    }
}

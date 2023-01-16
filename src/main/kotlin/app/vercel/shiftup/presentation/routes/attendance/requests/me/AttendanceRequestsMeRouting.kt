package app.vercel.shiftup.presentation.routes.attendance.requests.me

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.request.application.GetAfterNowAttendanceRequestAndSurveyUseCase
import app.vercel.shiftup.features.attendance.request.application.RespondAttendanceRequestUseCase
import app.vercel.shiftup.features.attendance.request.domain.model.value.AttendanceRequestState
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
        get<Requests.Divided> {
            @Serializable
            data class ResponseOpenCampusDateAndSurveyName(
                val openCampusDate: OpenCampusDate,
                val surveyName: String?,
            )

            @Serializable
            data class ResponseAttendanceRequest(
                val openCampusDate: OpenCampusDate,
                val state: AttendanceRequestState,
                val surveyName: String?,
            )

            @Serializable
            data class Response(
                val canRespondRequests: List<ResponseOpenCampusDateAndSurveyName>,
                val respondedRequests: List<ResponseAttendanceRequest>,
            )

            val useCase: GetAfterNowAttendanceRequestAndSurveyUseCase by inject()
            val requests = useCase(call.sessions.userId)
            val response = Response(
                canRespondRequests = requests.canRespondRequestAndSurveyList
                    .map { (request, survey) ->
                        ResponseOpenCampusDateAndSurveyName(
                            openCampusDate = request.openCampusDate,
                            surveyName = survey?.name,
                        )
                    },
                respondedRequests = requests.respondedRequestAndSurveyList
                    .map { (request, survey) ->
                        ResponseAttendanceRequest(
                            openCampusDate = request.openCampusDate,
                            state = request.state,
                            surveyName = survey?.name,
                        )
                    }
            )

            call.respond(response)
        }
    }
    post<Requests.Date.State> {
        val useCase: RespondAttendanceRequestUseCase by inject()
        useCase(
            userId = call.sessions.userId,
            openCampusDate = it.parent.openCampusDate,
            state = AttendanceRequestState.NonBlank(
                name = enumValueOf(call.receiveText()),
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
    @Resource("divided")
    class Divided(val parent: Requests)

    @Serializable
    @Resource("{openCampusDate}")
    class Date(val parent: Requests, val openCampusDate: OpenCampusDate) {
        @Serializable
        @Resource("state")
        class State(val parent: Date)
    }
}

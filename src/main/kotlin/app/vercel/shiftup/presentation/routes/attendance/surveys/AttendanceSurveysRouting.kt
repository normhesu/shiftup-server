package app.vercel.shiftup.presentation.routes.attendance.surveys

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.survey.answer.application.AddOrReplaceAttendanceSurveyAnswerUseCase
import app.vercel.shiftup.features.attendance.survey.application.*
import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurveyId
import app.vercel.shiftup.features.attendance.survey.domain.model.value.OpenCampusDates
import app.vercel.shiftup.features.user.account.domain.model.AvailableUser
import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.account.domain.model.value.Name
import app.vercel.shiftup.features.user.domain.model.value.Role
import app.vercel.shiftup.features.user.domain.model.value.SchoolProfile
import app.vercel.shiftup.features.user.invite.domain.model.value.Position
import app.vercel.shiftup.presentation.routes.attendance.Attendance
import app.vercel.shiftup.presentation.routes.attendance.surveys.me.attendanceSurveysMeRouting
import app.vercel.shiftup.presentation.routes.auth.plugins.routingWithRole
import app.vercel.shiftup.presentation.routes.auth.plugins.userId
import app.vercel.shiftup.presentation.routes.inject
import app.vercel.shiftup.presentation.routes.respondDeleteResult
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.mpierce.ktor.csrf.noCsrfProtection

fun Application.attendanceSurveysRouting() {
    castRouting()
    managerRouting()
    attendanceSurveysMeRouting()
}

private fun Application.castRouting() = routingWithRole(Role.Cast) {
    put<Surveys.Id.Answers> {
        val useCase: AddOrReplaceAttendanceSurveyAnswerUseCase by inject()
        useCase(
            attendanceSurveyId = it.parent.attendanceSurveyId,
            userId = call.sessions.userId,
            availableDays = call.receive(),
        ).onSuccess {
            call.respond(HttpStatusCode.NoContent)
        }.onFailure {
            call.response.headers.append(HttpHeaders.Allow, "")
            call.respond(HttpStatusCode.MethodNotAllowed)
        }
    }
}

private fun Application.managerRouting() = routingWithRole(Role.Manager) {
    noCsrfProtection {
        get<Surveys> {
            @Serializable
            data class ResponseItem(
                val id: AttendanceSurveyId,
                val name: String,
                val openCampusSchedule: OpenCampusDates,
                val creationDate: LocalDate,
                val available: Boolean,
                val answerCount: Int,
            )

            val useCase: GetCanSendAttendanceRequestAttendanceSurveyUseCase by inject()
            val response = useCase().map { (survey, answerCount) ->
                ResponseItem(
                    id = survey.id,
                    name = survey.name,
                    openCampusSchedule = survey.openCampusSchedule,
                    creationDate = survey.creationDate,
                    available = survey.available,
                    answerCount = answerCount,
                )
            }

            call.respond(response)
        }
    }

    post<Surveys> {
        @Serializable
        data class Params(
            val name: String,
            val openCampusSchedule: OpenCampusDates,
        )

        val useCase: AddAttendanceSurveyUseCase by inject()
        val (name, openCampusSchedule) = call.receive<Params>()
        useCase(name = name, openCampusSchedule = openCampusSchedule)

        call.respond(HttpStatusCode.Created)
    }

    delete<Surveys.Id> {
        val useCase: RemoveAttendanceSurveyUseCase by inject()
        call.respondDeleteResult(
            useCase(attendanceSurveyId = it.attendanceSurveyId)
        )
    }

    put<Surveys.Id.Available> {
        val useCase: ChangeAvailableAttendanceSurveyUseCase by inject()
        useCase(
            attendanceSurveyId = it.parent.attendanceSurveyId,
            available = call.receiveText().toBooleanStrict(),
        )

        call.respond(HttpStatusCode.NoContent)
    }

    surveyResultsRoute()
}

private fun Route.surveyResultsRoute() = noCsrfProtection {
    get<Surveys.Id.Result> { resource ->
        @Serializable
        data class ResponseCast(
            val id: UserId,
            val name: Name,
            val schoolProfile: SchoolProfile,
            val position: Position,
            val attendanceRequested: Boolean,
        ) {
            constructor(castWithAttendanceRequested: CastWithAttendanceRequested) : this(
                user = castWithAttendanceRequested.cast.value,
                attendanceRequested = castWithAttendanceRequested.attendanceRequested
            )

            // 上記のコンストラクタで使用されていても「コンストラクターは使用されません」という警告が出るので無視する
            @Suppress("unused")
            private constructor(user: AvailableUser, attendanceRequested: Boolean) : this(
                id = user.id,
                name = user.name,
                position = user.position,
                schoolProfile = user.schoolProfile,
                attendanceRequested = attendanceRequested,
            )
        }

        @Serializable
        data class ResponseItem(
            val date: OpenCampusDate,
            val tallied: Boolean,
            val availableCasts: Set<ResponseCast>,
        )

        @Serializable
        data class Response(
            val tallied: Boolean,
            val openCampuses: List<ResponseItem>,
        )

        val tallyUseCase: TallyAttendanceSurveyUseCase by inject()
        val tallyResult = tallyUseCase(resource.parent.attendanceSurveyId)

        val openCampuses = tallyResult.results.map {
            ResponseItem(
                date = it.openCampusDate,
                tallied = it.tallied,
                availableCasts = it.castsWithAttendanceRequested.map(::ResponseCast).toSet(),
            )
        }

        val response = Response(
            tallied = tallyResult.tallied,
            openCampuses = openCampuses,
        )

        call.respond(response)
    }
}

@Suppress("unused")
@Serializable
@Resource("surveys")
class Surveys(val parent: Attendance) {
    @Serializable
    @Resource("{id}")
    class Id(
        val parent: Surveys,
        val id: String,
    ) {
        val attendanceSurveyId get() = AttendanceSurveyId(id)

        @Serializable
        @Resource("available")
        class Available(val parent: Id)

        @Serializable
        @Resource("result")
        class Result(val parent: Id)

        @Serializable
        @Resource("answers")
        class Answers(val parent: Id)
    }
}

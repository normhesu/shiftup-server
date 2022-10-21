package app.vercel.shiftup.presentation.routes.attendancesurveys

import app.vercel.shiftup.features.attendancesurvey.answer.application.AddOrUpdateAttendanceSurveyAnswerUseCase
import app.vercel.shiftup.features.attendancesurvey.domain.model.value.AttendanceSurveyId
import app.vercel.shiftup.features.attendancesurvey.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendancesurvey.domain.model.value.OpenCampusDates
import app.vercel.shiftup.features.attendancesurvey.survey.application.*
import app.vercel.shiftup.features.user.account.application.GetUsersUseCase
import app.vercel.shiftup.features.user.account.domain.model.Cast
import app.vercel.shiftup.features.user.domain.model.value.Role
import app.vercel.shiftup.presentation.routes.auth.plugins.routingWithRole
import app.vercel.shiftup.presentation.routes.auth.plugins.userId
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
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

fun Application.attendanceSurveysRouting() {
    castRouting()
    managerRouting()
}

private fun Application.castRouting() = routingWithRole(Role.Cast) {
    put<AttendanceSurveys.Id.Answers> {
        val useCase: AddOrUpdateAttendanceSurveyAnswerUseCase
            by application.inject()

        useCase(
            attendanceSurveyId = it.parent.attendanceSurveyId,
            userId = requireNotNull(call.sessions.userId),
            availableDays = call.receive(),
        ).onSuccess {
            call.respond(HttpStatusCode.NoContent)
        }.onFailure {
            call.response.headers.append("Allow", "")
            call.respond(HttpStatusCode.MethodNotAllowed)
        }
    }
}

private fun Application.managerRouting() = routingWithRole(Role.Manager) {
    get<AttendanceSurveys> {
        val useCase: GetAllAttendanceSurveyUseCase
            by application.inject()

        call.respond(useCase())
    }

    post<AttendanceSurveys> {
        @Serializable
        data class Params(
            val name: String,
            val openCampusSchedule: OpenCampusDates,
        )

        val useCase: AddAttendanceSurveyUseCase
            by application.inject()
        val (name, openCampusSchedule) = call.receive<Params>()

        useCase(name = name, openCampusSchedule = openCampusSchedule)
        call.respond(HttpStatusCode.Created)
    }

    delete<AttendanceSurveys.Id> {
        val useCase: RemoveAttendanceSurveyUseCase
            by application.inject()

        call.respondDeleteResult(
            useCase(attendanceSurveyId = it.attendanceSurveyId)
        )
    }

    put<AttendanceSurveys.Id.Available> {
        @Serializable
        data class Params(val available: Boolean)

        val useCase: ChangeAvailableAttendanceSurveyUseCase
            by application.inject()

        useCase(
            attendanceSurveyId = it.parent.attendanceSurveyId,
            available = call.receive<Params>().available,
        )
        call.respond(HttpStatusCode.NoContent)
    }

    surveyResultsRoute()
}

private fun Route.surveyResultsRoute() = get<AttendanceSurveys.Id.Results> { resource ->
    @Serializable
    data class ResponseItem(
        val date: OpenCampusDate,
        val availableCasts: Set<Cast>,
    )

    val tallyUseCase: TallyAttendanceSurveyUseCase
        by application.inject()
    val getUsersUseCase: GetUsersUseCase
        by application.inject()

    val tallyResult = tallyUseCase(resource.parent.attendanceSurveyId)
    val availableCastUserIds = tallyResult
        .map { it.availableCastIds }
        .flatten()
        .distinct()
        .map { it.value }
    val casts = getUsersUseCase(availableCastUserIds)
        .map(::Cast)
        .associateBy { it.id }

    val result = tallyResult.map { openCampus ->
        ResponseItem(
            date = openCampus.date,
            availableCasts = openCampus.availableCastIds
                .mapNotNull { casts[it] }
                .toSet()
        )
    }
    call.respond(result)
}

@Suppress("unused")
@Serializable
@Resource("/surveys")
object AttendanceSurveys {
    @Serializable
    @Resource("{id}")
    class Id(
        val parent: AttendanceSurveys = AttendanceSurveys,
        val id: String,
    ) {
        val attendanceSurveyId get() = AttendanceSurveyId(id)

        @Serializable
        @Resource("available")
        class Available(val parent: Id)

        @Serializable
        @Resource("results")
        class Results(val parent: Id)

        @Serializable
        @Resource("answers")
        class Answers(val parent: Id)
    }
}

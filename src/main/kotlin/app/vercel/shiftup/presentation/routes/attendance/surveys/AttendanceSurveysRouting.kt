package app.vercel.shiftup.presentation.routes.attendance.surveys

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.survey.application.*
import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurveyId
import app.vercel.shiftup.features.attendance.survey.domain.model.value.OpenCampusDates
import app.vercel.shiftup.features.user.account.application.GetUsersUseCase
import app.vercel.shiftup.features.user.account.domain.model.Cast
import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.account.domain.model.value.Name
import app.vercel.shiftup.features.user.domain.model.value.Role
import app.vercel.shiftup.features.user.domain.model.value.SchoolProfile
import app.vercel.shiftup.features.user.invite.domain.model.value.Position
import app.vercel.shiftup.presentation.routes.attendance.Attendance
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
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import org.mpierce.ktor.csrf.noCsrfProtection

fun Application.attendanceSurveysRouting() {
    castRouting()
    managerRouting()
}

private fun Application.castRouting() = routingWithRole(Role.Cast) {
    put<Surveys.Id.Answers> {
        val useCase: AddOrReplaceAttendanceSurveyAnswerUseCase
            by application.inject()

        useCase(
            attendanceSurveyId = it.parent.attendanceSurveyId,
            userId = requireNotNull(call.sessions.userId),
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
            )

            val useCase: GetAllAttendanceSurveyUseCase
                by application.inject()

            val response = useCase().map {
                ResponseItem(
                    id = it.id,
                    name = it.name,
                    openCampusSchedule = it.openCampusSchedule,
                    creationDate = it.creationDate,
                    available = it.available,
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

        val useCase: AddAttendanceSurveyUseCase
            by application.inject()
        val (name, openCampusSchedule) = call.receive<Params>()

        useCase(name = name, openCampusSchedule = openCampusSchedule)
        call.respond(HttpStatusCode.Created)
    }

    delete<Surveys.Id> {
        val useCase: RemoveAttendanceSurveyUseCase
            by application.inject()

        call.respondDeleteResult(
            useCase(attendanceSurveyId = it.attendanceSurveyId)
        )
    }

    put<Surveys.Id.Available> {
        val useCase: ChangeAvailableAttendanceSurveyUseCase
            by application.inject()

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
            val available: Boolean,
        )

        @Serializable
        data class ResponseItem(
            val date: OpenCampusDate,
            val availableCasts: Set<ResponseCast>,
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
            .mapValues { (_, cast) ->
                val user = cast.value
                ResponseCast(
                    id = user.id,
                    name = user.name,
                    schoolProfile = user.schoolProfile,
                    position = user.position,
                    available = user.available,
                )
            }

        val response = tallyResult.map { openCampus ->
            ResponseItem(
                date = openCampus.date,
                availableCasts = openCampus.availableCastIds
                    .mapNotNull { casts[it] }
                    .toSet()
            )
        }

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

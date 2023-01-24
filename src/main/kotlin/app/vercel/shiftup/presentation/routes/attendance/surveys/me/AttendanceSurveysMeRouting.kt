package app.vercel.shiftup.presentation.routes.attendance.surveys.me

import app.vercel.shiftup.features.attendance.survey.application.GetCanAnswerAttendanceSurveyAndAnswerListUseCase
import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurveyId
import app.vercel.shiftup.features.attendance.survey.domain.model.value.SameFiscalYearOpenCampusDates
import app.vercel.shiftup.features.user.domain.model.value.Role
import app.vercel.shiftup.presentation.routes.auth.plugins.routingWithRole
import app.vercel.shiftup.presentation.routes.auth.plugins.userId
import app.vercel.shiftup.presentation.routes.inject
import app.vercel.shiftup.presentation.routes.users.Users
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.mpierce.ktor.csrf.noCsrfProtection

fun Application.attendanceSurveysMeRouting() = routingWithRole(Role.Cast) {
    noCsrfProtection {
        get<Surveys> {
            @Serializable
            data class ResponseItem(
                val id: AttendanceSurveyId,
                val name: String,
                val openCampusSchedule: SameFiscalYearOpenCampusDates,
                val creationDate: LocalDate,
                val answer: SameFiscalYearOpenCampusDates,
            )

            val useCase: GetCanAnswerAttendanceSurveyAndAnswerListUseCase by inject()
            val response = useCase(call.sessions.userId).map { (survey, answer) ->
                ResponseItem(
                    id = survey.id,
                    name = survey.name,
                    openCampusSchedule = survey.openCampusSchedule,
                    creationDate = survey.creationDate,
                    answer = answer?.availableDays ?: SameFiscalYearOpenCampusDates.empty(),
                )
            }
            call.respond(response)
        }
    }
}

@Suppress("unused")
@Serializable
@Resource("surveys")
class Surveys(val parent: Users.Me.Attendance)

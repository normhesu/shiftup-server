package app.vercel.shiftup.presentation.routes.attendance.surveys.scheduling

import app.vercel.shiftup.features.attendance.survey.answer.application.RemoveNoAttendanceSurveyExistsAttendanceSurveyAnswerUseCase
import app.vercel.shiftup.features.attendance.survey.application.RemoveAfterOpenCampusPeriodAttendanceSurveyUseCase
import app.vercel.shiftup.presentation.plugins.schedule
import io.ktor.server.application.*
import org.koin.ktor.ext.inject
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

fun Application.attendanceSurveysScheduling() {
    schedule(
        name = "RemoveAfterOpenCampusPeriodAttendanceSurvey",
        fixedDelay = 1.days,
        runStartup = true,
    ) {
        val useCase: RemoveAfterOpenCampusPeriodAttendanceSurveyUseCase by inject()
        useCase().also {
            log.info("deletedAttendanceSurveyCount ${it.deletedCount}")
        }
    }
    schedule(
        name = "RemoveNoAttendanceSurveyExistsAttendanceSurveyAnswer",
        fixedDelay = 12.hours,
        runStartup = true,
    ) {
        val useCase: RemoveNoAttendanceSurveyExistsAttendanceSurveyAnswerUseCase by inject()
        useCase().also {
            log.info("deletedAttendanceSurveyAnswerCount ${it.deletedCount}")
        }
    }
}

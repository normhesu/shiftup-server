package app.vercel.shiftup.presentation.plugins

import app.vercel.shiftup.features.attendance.request.application.RemoveAfterOpenCampusDateAttendanceRequestUseCase
import app.vercel.shiftup.features.attendance.survey.answer.application.RemoveNoAttendanceSurveyExistsAttendanceSurveyAnswerUseCase
import app.vercel.shiftup.features.attendance.survey.application.RemoveAfterOpenCampusPeriodAttendanceSurveyUseCase
import io.ktor.server.application.*
import io.ktor.util.logging.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

fun Application.configureScheduling() {
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
    schedule(
        name = "RemoveAfterOpenCampusDateAttendanceRequest",
        fixedDelay = 1.days,
        runStartup = true,
    ) {
        val useCase: RemoveAfterOpenCampusDateAttendanceRequestUseCase by inject()
        useCase().also {
            log.info("deletedAttendanceRequestCount ${it.deletedCount}")
        }
    }
}

private fun Application.schedule(
    name: String,
    fixedDelay: Duration,
    runStartup: Boolean = false,
    task: suspend () -> Unit,
) = launch {
    if (!runStartup) delay(fixedDelay)
    while (true) {
        invokeTask(name, task)
        delay(fixedDelay)
    }
}

private suspend fun Application.invokeTask(
    name: String,
    task: suspend () -> Unit,
) = runCatching {
    log.info("Run $name")
    task()
}.onSuccess {
    log.info("Success $name")
}.onFailure {
    when (it) {
        is CancellationException -> {
            log.info("Cancel $name")
            throw it
        }

        else -> {
            // CancellationException以外が投げられても終了しない
            log.error("Failed $name")
            log.error(it)
        }
    }
}

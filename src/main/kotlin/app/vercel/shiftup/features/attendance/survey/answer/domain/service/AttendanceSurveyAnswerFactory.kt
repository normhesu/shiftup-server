package app.vercel.shiftup.features.attendance.survey.answer.domain.service

import app.vercel.shiftup.features.attendance.survey.answer.domain.model.AttendanceSurveyAnswer
import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurveyId
import app.vercel.shiftup.features.attendance.survey.domain.model.value.OpenCampusDates
import app.vercel.shiftup.features.user.account.domain.model.Cast
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import org.koin.core.annotation.Single

@Single
class AttendanceSurveyAnswerFactory(
    private val attendanceSurveyRepository: AttendanceSurveyRepositoryInterface,
) {
    suspend operator fun invoke(
        attendanceSurveyId: AttendanceSurveyId,
        cast: Cast,
        availableDays: OpenCampusDates,
    ): Result<AttendanceSurveyAnswer, AttendanceSurveyAnswerFactoryException> = runSuspendCatching {
        val survey = attendanceSurveyRepository.findById(
            attendanceSurveyId,
        )
        when {
            survey == null -> throw AttendanceSurveyAnswerFactoryException.NotFoundSurvey
            survey.available.not() -> throw AttendanceSurveyAnswerFactoryException.NotAvailableSurvey
            else -> {
                require(cast.inSchool(survey.fiscalYear))
                require(availableDays.all { it in survey.openCampusSchedule })
            }
        }
        AttendanceSurveyAnswer.fromFactory(
            surveyId = attendanceSurveyId,
            availableCastId = cast.id,
            availableDays = availableDays,
        )
    }.mapError {
        when (it) {
            is AttendanceSurveyAnswerFactoryException -> it
            else -> throw it
        }
    }
}

sealed interface AttendanceSurveyAnswerFactoryException {
    object NotFoundSurvey : Exception(), AttendanceSurveyAnswerFactoryException
    object NotAvailableSurvey : Exception(), AttendanceSurveyAnswerFactoryException
}

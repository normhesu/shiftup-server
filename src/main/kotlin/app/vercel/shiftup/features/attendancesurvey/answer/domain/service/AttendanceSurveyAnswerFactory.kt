package app.vercel.shiftup.features.attendancesurvey.answer.domain.service

import app.vercel.shiftup.features.attendancesurvey.answer.domain.model.AttendanceSurveyAnswer
import app.vercel.shiftup.features.attendancesurvey.domain.model.value.AttendanceSurveyId
import app.vercel.shiftup.features.attendancesurvey.domain.model.value.OpenCampusDates
import app.vercel.shiftup.features.attendancesurvey.survey.domain.service.AttendanceSurveyRepositoryInterface
import app.vercel.shiftup.features.user.account.domain.model.Cast
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.runCatching
import org.koin.core.annotation.Single

@Single
class AttendanceSurveyAnswerFactory(
    private val attendanceSurveyRepository: AttendanceSurveyRepositoryInterface,
) {
    suspend operator fun invoke(
        attendanceSurveyId: AttendanceSurveyId,
        cast: Cast,
        availableDays: OpenCampusDates,
    ): Result<AttendanceSurveyAnswer, AttendanceSurveyAnswerFactoryException> = runCatching {
        attendanceSurveyRepository.findById(
            attendanceSurveyId,
        ).also { survey ->
            when {
                survey == null -> throw AttendanceSurveyAnswerFactoryException.NotFoundSurvey()
                survey.isAvailable.not() -> throw AttendanceSurveyAnswerFactoryException.NotAvailableSurvey()
                else -> {
                    require(cast.inSchool(survey.fiscalYear))
                    require(availableDays.all { it in survey.openCampusSchedule })
                }
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
    class NotFoundSurvey : Exception(), AttendanceSurveyAnswerFactoryException
    class NotAvailableSurvey : Exception(), AttendanceSurveyAnswerFactoryException
}

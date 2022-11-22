package app.vercel.shiftup.features.attendancesurvey.domain.service

import app.vercel.shiftup.features.attendancesurvey.domain.model.AttendanceSurveyId
import app.vercel.shiftup.features.attendancesurvey.domain.model.value.AttendanceSurveyAnswer
import app.vercel.shiftup.features.attendancesurvey.domain.model.value.OpenCampusDates
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
        attendanceSurveyRepository.findById(
            attendanceSurveyId,
        ).also { survey ->
            when {
                survey == null -> throw AttendanceSurveyAnswerFactoryException.NotFoundSurvey()
                survey.available.not() -> throw AttendanceSurveyAnswerFactoryException.NotAvailableSurvey()
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

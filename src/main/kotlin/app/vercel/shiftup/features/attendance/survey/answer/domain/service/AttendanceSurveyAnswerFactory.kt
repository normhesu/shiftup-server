package app.vercel.shiftup.features.attendance.survey.answer.domain.service

import app.vercel.shiftup.features.attendance.survey.answer.domain.model.AttendanceSurveyAnswer
import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurveyId
import app.vercel.shiftup.features.attendance.survey.domain.model.value.SameFiscalYearOpenCampusDates
import app.vercel.shiftup.features.attendance.survey.domain.service.AttendanceSurveyRepositoryInterface
import app.vercel.shiftup.features.user.account.domain.model.Cast
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import org.koin.core.annotation.Single

@Single
class AttendanceSurveyAnswerFactory(
    private val attendanceSurveyRepository: AttendanceSurveyRepositoryInterface,
) {
    suspend operator fun invoke(
        attendanceSurveyId: AttendanceSurveyId,
        cast: Cast,
        availableDays: SameFiscalYearOpenCampusDates,
    ): Result<AttendanceSurveyAnswer, AttendanceSurveyAnswerFactoryException> {
        val survey = attendanceSurveyRepository.findById(
            attendanceSurveyId,
        )
        when {
            survey == null -> return Err(AttendanceSurveyAnswerFactoryException.NotFoundSurvey)
            survey.canAnswer(cast).not() -> return Err(AttendanceSurveyAnswerFactoryException.CanNotAnswer)
            else -> {
                require(availableDays.all { it in survey.openCampusSchedule })
            }
        }
        return AttendanceSurveyAnswer.fromFactory(
            surveyId = attendanceSurveyId,
            availableCastId = cast.id,
            availableDays = availableDays,
        ).let(::Ok)
    }
}

sealed interface AttendanceSurveyAnswerFactoryException {
    object NotFoundSurvey : Exception(), AttendanceSurveyAnswerFactoryException
    object CanNotAnswer : Exception(), AttendanceSurveyAnswerFactoryException
}

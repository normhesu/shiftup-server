package app.vercel.shiftup.features.attendance.survey.application

import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurveyId
import app.vercel.shiftup.features.attendance.survey.infra.AttendanceSurveyRepository
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import org.koin.core.annotation.Single

@Single
class ChangeAvailableAttendanceSurveyUseCase(
    private val attendanceSurveyRepository: AttendanceSurveyRepository,
) {
    suspend operator fun invoke(
        attendanceSurveyId: AttendanceSurveyId,
        available: Boolean,
    ): Result<Unit, ChangeAvailableAttendanceSurveyUseCaseException> {
        val survey = attendanceSurveyRepository.findById(attendanceSurveyId) ?: return Err(
            ChangeAvailableAttendanceSurveyUseCaseException.NotFoundSurvey,
        )
        if (survey.available == available) return Ok(Unit)

        attendanceSurveyRepository.replace(
            survey.changeAvailable(available = available),
        )
        return Ok(Unit)
    }
}

sealed class ChangeAvailableAttendanceSurveyUseCaseException : Exception() {
    object NotFoundSurvey : ChangeAvailableAttendanceSurveyUseCaseException()
}

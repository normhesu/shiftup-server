package app.vercel.shiftup.features.attendance.survey.application

import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurveyId
import app.vercel.shiftup.features.attendance.survey.infra.AttendanceSurveyRepository
import io.ktor.server.plugins.*
import org.koin.core.annotation.Single

@Single
class ChangeAvailableAttendanceSurveyUseCase(
    private val attendanceSurveyRepository: AttendanceSurveyRepository,
) {
    suspend operator fun invoke(
        attendanceSurveyId: AttendanceSurveyId,
        available: Boolean,
    ) {
        val survey = attendanceSurveyRepository.findById(attendanceSurveyId) ?: throw NotFoundException()
        if (survey.available == available) return

        attendanceSurveyRepository.replace(
            survey.changeAvailable(available = available),
        )
    }
}

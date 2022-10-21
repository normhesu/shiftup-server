package app.vercel.shiftup.features.attendancesurvey.survey.application

import app.vercel.shiftup.features.attendancesurvey.domain.model.value.AttendanceSurveyId
import app.vercel.shiftup.features.attendancesurvey.survey.infra.AttendanceSurveyRepository
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
        if (survey.isAvailable == available) return

        attendanceSurveyRepository.replace(
            survey.changeAvailable(available = available),
        )
    }
}

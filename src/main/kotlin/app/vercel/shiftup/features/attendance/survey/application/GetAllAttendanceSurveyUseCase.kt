package app.vercel.shiftup.features.attendance.survey.application

import app.vercel.shiftup.features.attendance.survey.infra.AttendanceSurveyRepository
import org.koin.core.annotation.Single

@Single
class GetAllAttendanceSurveyUseCase(
    private val attendanceSurveyRepository: AttendanceSurveyRepository,
) {
    suspend operator fun invoke() = attendanceSurveyRepository.findAll()
}

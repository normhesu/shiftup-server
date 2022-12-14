package app.vercel.shiftup.features.attendance.survey.application

import app.vercel.shiftup.features.attendance.survey.infra.AttendanceSurveyRepository
import com.mongodb.client.result.DeleteResult
import org.koin.core.annotation.Single

@Single
class RemoveAfterOpenCampusPeriodAttendanceSurveyUseCase(
    private val attendanceSurveyRepository: AttendanceSurveyRepository,
) {
    suspend operator fun invoke(): DeleteResult = attendanceSurveyRepository.findAll()
        .filter { it.isAfterOpenCampusPeriod() }
        .map { it.id }
        .let {
            attendanceSurveyRepository.removeAllById(it)
        }
}

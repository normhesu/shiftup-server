package app.vercel.shiftup.features.attendance.survey.application

import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurveyId
import app.vercel.shiftup.features.attendance.survey.infra.AttendanceSurveyRepository
import com.mongodb.client.result.DeleteResult
import org.koin.core.annotation.Single

@Single
class RemoveAttendanceSurveyUseCase(
    private val attendanceSurveyRepository: AttendanceSurveyRepository,
) {
    suspend operator fun invoke(attendanceSurveyId: AttendanceSurveyId): DeleteResult {
        return attendanceSurveyRepository.remove(attendanceSurveyId)
    }
}

package app.vercel.shiftup.features.attendancesurvey.application

import app.vercel.shiftup.features.attendancesurvey.domain.model.AttendanceSurveyId
import app.vercel.shiftup.features.attendancesurvey.infra.AttendanceSurveyRepository
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

package app.vercel.shiftup.features.attendancesurvey.application

import app.vercel.shiftup.features.attendancesurvey.domain.model.AttendanceSurveyId
import app.vercel.shiftup.features.attendancesurvey.domain.model.value.OpenCampus
import app.vercel.shiftup.features.attendancesurvey.domain.service.AttendanceSurveyRepositoryInterface
import io.ktor.server.plugins.*
import org.koin.core.annotation.Single

@Single
class TallyAttendanceSurveyUseCase(
    private val attendanceSurveyRepository: AttendanceSurveyRepositoryInterface,
) {
    suspend operator fun invoke(surveyId: AttendanceSurveyId): Set<OpenCampus> {
        val survey = attendanceSurveyRepository.findById(surveyId) ?: throw NotFoundException()
        return survey.tally()
    }
}

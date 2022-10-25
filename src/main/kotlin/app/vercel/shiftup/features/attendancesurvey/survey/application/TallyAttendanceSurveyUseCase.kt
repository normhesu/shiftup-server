package app.vercel.shiftup.features.attendancesurvey.survey.application

import app.vercel.shiftup.features.attendancesurvey.answer.infra.AttendanceSurveyAnswerRepository
import app.vercel.shiftup.features.attendancesurvey.domain.model.value.AttendanceSurveyId
import app.vercel.shiftup.features.attendancesurvey.survey.domain.model.value.OpenCampus
import app.vercel.shiftup.features.attendancesurvey.survey.domain.service.AttendanceSurveyRepositoryInterface
import io.ktor.server.plugins.*
import org.koin.core.annotation.Single

@Single
class TallyAttendanceSurveyUseCase(
    private val attendanceSurveyRepository: AttendanceSurveyRepositoryInterface,
    private val attendanceSurveyAnswerRepository: AttendanceSurveyAnswerRepository,
) {
    suspend operator fun invoke(surveyId: AttendanceSurveyId): Set<OpenCampus> {
        val survey = attendanceSurveyRepository.findById(surveyId) ?: throw NotFoundException()
        val answers = attendanceSurveyAnswerRepository.findBySurveyId(surveyId)
        return survey.tally(answers)
    }
}

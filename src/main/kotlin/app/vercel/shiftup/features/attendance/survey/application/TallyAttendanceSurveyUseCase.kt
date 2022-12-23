package app.vercel.shiftup.features.attendance.survey.application

import app.vercel.shiftup.features.attendance.survey.answer.domain.service.AttendanceSurveyRepositoryInterface
import app.vercel.shiftup.features.attendance.survey.answer.infra.AttendanceSurveyAnswerRepository
import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurveyId
import app.vercel.shiftup.features.attendance.survey.domain.model.value.OpenCampus
import io.ktor.server.plugins.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.koin.core.annotation.Single

@Single
class TallyAttendanceSurveyUseCase(
    private val attendanceSurveyRepository: AttendanceSurveyRepositoryInterface,
    private val attendanceSurveyAnswerRepository: AttendanceSurveyAnswerRepository,
) {
    suspend operator fun invoke(surveyId: AttendanceSurveyId): Set<OpenCampus> = coroutineScope {
        val surveyDeferred = async { attendanceSurveyRepository.findById(surveyId) ?: throw NotFoundException() }
        val answersDeferred = async { attendanceSurveyAnswerRepository.findBySurveyId(surveyId) }
        surveyDeferred.await().tally(
            answersDeferred.await()
        )
    }
}

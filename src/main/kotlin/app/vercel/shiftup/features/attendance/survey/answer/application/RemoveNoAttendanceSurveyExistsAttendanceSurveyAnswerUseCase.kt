package app.vercel.shiftup.features.attendance.survey.answer.application

import app.vercel.shiftup.features.attendance.survey.answer.infra.AttendanceSurveyAnswerRepository
import app.vercel.shiftup.features.attendance.survey.infra.AttendanceSurveyRepository
import com.mongodb.client.result.DeleteResult
import org.koin.core.annotation.Single

@Single
class RemoveNoAttendanceSurveyExistsAttendanceSurveyAnswerUseCase(
    private val attendanceSurveyRepository: AttendanceSurveyRepository,
    private val attendanceSurveyAnswerRepository: AttendanceSurveyAnswerRepository,
) {
    suspend operator fun invoke(): DeleteResult {
        val surveyIds = attendanceSurveyRepository.findAll().map { it.id }
        return attendanceSurveyAnswerRepository.removeNotContainsSurveyId(surveyIds)
    }
}

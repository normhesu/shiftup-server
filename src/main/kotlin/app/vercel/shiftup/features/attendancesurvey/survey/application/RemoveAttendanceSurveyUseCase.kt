package app.vercel.shiftup.features.attendancesurvey.survey.application

import app.vercel.shiftup.features.attendancesurvey.answer.infra.AttendanceSurveyAnswerRepository
import app.vercel.shiftup.features.attendancesurvey.domain.model.value.AttendanceSurveyId
import app.vercel.shiftup.features.attendancesurvey.survey.infra.AttendanceSurveyRepository
import app.vercel.shiftup.features.core.infra.Transaction
import com.mongodb.client.result.DeleteResult
import org.koin.core.annotation.Single

@Single
class RemoveAttendanceSurveyUseCase(
    private val attendanceSurveyRepository: AttendanceSurveyRepository,
    private val attendanceSurveyAnswerRepository: AttendanceSurveyAnswerRepository,
    private val transaction: Transaction,
) {
    suspend operator fun invoke(
        attendanceSurveyId: AttendanceSurveyId,
    ): DeleteResult = transaction {
        val result = attendanceSurveyRepository.remove(attendanceSurveyId)
        attendanceSurveyAnswerRepository.removeBySurveyId(attendanceSurveyId)
        result
    }
}

package app.vercel.shiftup.features.attendance.survey.application

import app.vercel.shiftup.features.attendance.survey.infra.AttendanceSurveyRepository
import com.mongodb.client.result.DeleteResult
import org.koin.core.annotation.Single

@Single
class RemoveAfterOpenCampusPeriodAttendanceSurveyUseCase(
    private val attendanceSurveyRepository: AttendanceSurveyRepository,
) {
    suspend operator fun invoke(): DeleteResult {
        val surveyIds = attendanceSurveyRepository.findAll()
            .filter {
                // アンケートが大量に保存されることはないため、取得後にfilterをする
                it.isAfterOpenCampusPeriod()
            }
            .map { it.id }

        return attendanceSurveyRepository.removeAllById(surveyIds)
    }
}

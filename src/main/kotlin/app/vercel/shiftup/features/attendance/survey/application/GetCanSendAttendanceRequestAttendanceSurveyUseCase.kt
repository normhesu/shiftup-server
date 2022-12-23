package app.vercel.shiftup.features.attendance.survey.application

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.survey.answer.infra.AttendanceSurveyAnswerRepository
import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurvey
import app.vercel.shiftup.features.attendance.survey.infra.AttendanceSurveyRepository
import org.koin.core.annotation.Single

@Single
class GetCanSendAttendanceRequestAttendanceSurveyUseCase(
    private val attendanceSurveyRepository: AttendanceSurveyRepository,
    private val attendanceSurveyAnswerRepository: AttendanceSurveyAnswerRepository,
) {
    suspend operator fun invoke(): List<GetCanSendAttendanceRequestAttendanceSurveyUseCaseResultItem> {
        val allSurveys = attendanceSurveyRepository.findAll()
        val canSendAttendanceRequestSurveys = run {
            val now = OpenCampusDate.now()
            // アンケートが大量に保存されることはないため、取得後にfilterをする
            allSurveys.filter {
                it.canSendAttendanceRequest(now)
            }
        }
        val answerCounts = attendanceSurveyAnswerRepository.countBySurveyIds(
            canSendAttendanceRequestSurveys.map { it.id }
        )

        return canSendAttendanceRequestSurveys.map {
            GetCanSendAttendanceRequestAttendanceSurveyUseCaseResultItem(
                attendanceSurvey = it,
                answerCount = answerCounts[it.id] ?: 0,
            )
        }
    }
}

data class GetCanSendAttendanceRequestAttendanceSurveyUseCaseResultItem(
    val attendanceSurvey: AttendanceSurvey,
    val answerCount: Int,
)

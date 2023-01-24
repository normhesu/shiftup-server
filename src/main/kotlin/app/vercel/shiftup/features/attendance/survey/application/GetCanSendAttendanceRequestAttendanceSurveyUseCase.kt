package app.vercel.shiftup.features.attendance.survey.application

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.request.infra.AttendanceRequestRepository
import app.vercel.shiftup.features.attendance.survey.answer.infra.AttendanceSurveyAnswerRepository
import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurvey
import app.vercel.shiftup.features.attendance.survey.infra.AttendanceSurveyRepository
import org.koin.core.annotation.Single

@Single
class GetCanSendAttendanceRequestAttendanceSurveyUseCase(
    private val attendanceSurveyRepository: AttendanceSurveyRepository,
    private val attendanceSurveyAnswerRepository: AttendanceSurveyAnswerRepository,
    private val attendanceRequestRepository: AttendanceRequestRepository,
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

        val requestOpenCampusDates = attendanceRequestRepository.findByOpenCampusDateCollection(
            canSendAttendanceRequestSurveys.flatMap { it.openCampusSchedule },
        ).map {
            it.openCampusDate
        }

        val answerCounts = attendanceSurveyAnswerRepository.countBySurveyIds(
            canSendAttendanceRequestSurveys.map { it.id }
        )

        return canSendAttendanceRequestSurveys.map { survey ->
            GetCanSendAttendanceRequestAttendanceSurveyUseCaseResultItem(
                attendanceSurvey = survey,
                answerCount = answerCounts[survey.id] ?: 0,
                canDelete = survey.openCampusSchedule.all { it !in requestOpenCampusDates },
            )
        }
    }
}

data class GetCanSendAttendanceRequestAttendanceSurveyUseCaseResultItem(
    val attendanceSurvey: AttendanceSurvey,
    val answerCount: Int,
    val canDelete: Boolean,
)

package app.vercel.shiftup.features.attendance.survey.application

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurvey
import app.vercel.shiftup.features.attendance.survey.infra.AttendanceSurveyRepository
import org.koin.core.annotation.Single

@Single
class GetCanSendAttendanceRequestAttendanceSurveyUseCase(
    private val attendanceSurveyRepository: AttendanceSurveyRepository,
) {
    suspend operator fun invoke(): List<AttendanceSurvey> {
        val now = OpenCampusDate.now()
        return attendanceSurveyRepository.findAll().filter {
            it.openCampusSchedule.laterDateOrThrow() >= now
        }
    }
}

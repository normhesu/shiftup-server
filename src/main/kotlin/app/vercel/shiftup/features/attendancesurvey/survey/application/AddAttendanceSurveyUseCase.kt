package app.vercel.shiftup.features.attendancesurvey.survey.application

import app.vercel.shiftup.features.attendancesurvey.domain.model.value.OpenCampusDates
import app.vercel.shiftup.features.attendancesurvey.survey.domain.model.AttendanceSurvey
import app.vercel.shiftup.features.attendancesurvey.survey.infra.AttendanceSurveyRepository
import org.koin.core.annotation.Single

@Single
class AddAttendanceSurveyUseCase(
    private val attendanceSurveyRepository: AttendanceSurveyRepository,
) {
    suspend operator fun invoke(
        name: String,
        openCampusSchedule: OpenCampusDates,
    ) = attendanceSurveyRepository.addSurvey(
        AttendanceSurvey(
            name = name,
            openCampusSchedule = openCampusSchedule,
        )
    )
}

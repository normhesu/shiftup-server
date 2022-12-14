package app.vercel.shiftup.features.attendance.survey.application

import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurvey
import app.vercel.shiftup.features.attendance.survey.domain.model.value.OpenCampusDates
import app.vercel.shiftup.features.attendance.survey.infra.AttendanceSurveyRepository
import org.koin.core.annotation.Single

@Single
class AddAttendanceSurveyUseCase(
    private val attendanceSurveyRepository: AttendanceSurveyRepository,
) {
    suspend operator fun invoke(
        name: String,
        openCampusSchedule: OpenCampusDates,
    ) = attendanceSurveyRepository.add(
        AttendanceSurvey(
            name = name,
            openCampusSchedule = openCampusSchedule,
        )
    )
}

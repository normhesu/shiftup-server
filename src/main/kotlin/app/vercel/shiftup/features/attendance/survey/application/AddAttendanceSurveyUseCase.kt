package app.vercel.shiftup.features.attendance.survey.application

import app.vercel.shiftup.features.attendance.survey.domain.model.value.SameFiscalYearOpenCampusDates
import app.vercel.shiftup.features.attendance.survey.domain.service.AttendanceSurveyFactory
import app.vercel.shiftup.features.attendance.survey.infra.AttendanceSurveyRepository
import org.koin.core.annotation.Single

@Single
class AddAttendanceSurveyUseCase(
    private val attendanceSurveyRepository: AttendanceSurveyRepository,
    private val attendanceSurveyFactory: AttendanceSurveyFactory,
) {
    suspend operator fun invoke(
        name: String,
        openCampusSchedule: SameFiscalYearOpenCampusDates,
    ) = attendanceSurveyRepository.add(
        attendanceSurveyFactory(
            name = name,
            openCampusSchedule = openCampusSchedule,
        )
    )
}

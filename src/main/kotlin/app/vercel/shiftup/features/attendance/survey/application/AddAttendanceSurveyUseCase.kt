package app.vercel.shiftup.features.attendance.survey.application

import app.vercel.shiftup.features.attendance.survey.domain.model.value.SameFiscalYearOpenCampusDates
import app.vercel.shiftup.features.attendance.survey.domain.service.AttendanceSurveyFactory
import app.vercel.shiftup.features.attendance.survey.infra.AttendanceSurveyRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single

@Single
class AddAttendanceSurveyUseCase(
    private val attendanceSurveyRepository: AttendanceSurveyRepository,
    private val attendanceSurveyFactory: AttendanceSurveyFactory,
) {
    companion object {
        private val mutex = Mutex()
    }

    suspend operator fun invoke(
        name: String,
        openCampusSchedule: SameFiscalYearOpenCampusDates,
    ) = mutex.withLock {
        val survey = attendanceSurveyFactory(
            name = name,
            openCampusSchedule = openCampusSchedule,
        )
        attendanceSurveyRepository.add(survey)
    }
}

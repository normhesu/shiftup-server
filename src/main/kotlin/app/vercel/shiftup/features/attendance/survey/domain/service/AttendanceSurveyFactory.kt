package app.vercel.shiftup.features.attendance.survey.domain.service

import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurvey
import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurveyId
import app.vercel.shiftup.features.attendance.survey.domain.model.value.OpenCampusDates
import app.vercel.shiftup.features.attendance.survey.domain.model.value.flatten
import app.vercel.shiftup.features.core.domain.model.toTokyoLocalDateTime
import kotlinx.datetime.Clock
import org.koin.core.annotation.Single

@Single
class AttendanceSurveyFactory(
    private val attendanceSurveyRepository: AttendanceSurveyRepositoryInterface,
) {
    suspend operator fun invoke(
        name: String,
        openCampusSchedule: OpenCampusDates,
    ): AttendanceSurvey {
        val surveys = attendanceSurveyRepository.findAll()
        val duplicateOpenCampusDate = surveys
            .map { it.openCampusSchedule }
            .flatten()
            .any { it in openCampusSchedule }

        require(!duplicateOpenCampusDate)

        val id = AttendanceSurveyId()
        return AttendanceSurvey.fromFactory(
            name = name,
            openCampusSchedule = openCampusSchedule,
            creationDate = Clock.System.now().toTokyoLocalDateTime().date,
            available = true,
            id = id,
        )
    }
}

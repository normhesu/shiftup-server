package app.vercel.shiftup.features.attendancesurvey.survey.domain.service

import app.vercel.shiftup.features.attendancesurvey.domain.model.value.AttendanceSurveyId
import app.vercel.shiftup.features.attendancesurvey.survey.domain.model.AttendanceSurvey

interface AttendanceSurveyRepositoryInterface {
    suspend fun findById(attendanceSurveyId: AttendanceSurveyId): AttendanceSurvey?
}

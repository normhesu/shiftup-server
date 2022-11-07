package app.vercel.shiftup.features.attendancesurvey.domain.service

import app.vercel.shiftup.features.attendancesurvey.domain.model.AttendanceSurvey
import app.vercel.shiftup.features.attendancesurvey.domain.model.AttendanceSurveyId

interface AttendanceSurveyRepositoryInterface {
    suspend fun findById(attendanceSurveyId: AttendanceSurveyId): AttendanceSurvey?
}

package app.vercel.shiftup.features.attendance.survey.domain.service

import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurvey
import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurveyId

interface AttendanceSurveyRepositoryInterface {
    suspend fun findById(attendanceSurveyId: AttendanceSurveyId): AttendanceSurvey?
}

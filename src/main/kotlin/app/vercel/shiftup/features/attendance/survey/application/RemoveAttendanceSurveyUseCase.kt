package app.vercel.shiftup.features.attendance.survey.application

import app.vercel.shiftup.features.attendance.application.removeAttendanceSurveyUseCaseAndApplyAttendanceRequestToOpenCampusDateUseCaseMutex
import app.vercel.shiftup.features.attendance.request.infra.AttendanceRequestRepository
import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurveyId
import app.vercel.shiftup.features.attendance.survey.infra.AttendanceSurveyRepository
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.mongodb.client.result.DeleteResult
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single

@Single
class RemoveAttendanceSurveyUseCase(
    private val attendanceSurveyRepository: AttendanceSurveyRepository,
    private val attendanceRequestRepository: AttendanceRequestRepository,
) {
    suspend operator fun invoke(
        attendanceSurveyId: AttendanceSurveyId,
    ): Result<DeleteResult, RemoveAttendanceSurveyUseCaseException> =
        removeAttendanceSurveyUseCaseAndApplyAttendanceRequestToOpenCampusDateUseCaseMutex.withLock {
            val survey = attendanceSurveyRepository.findById(attendanceSurveyId).let(::checkNotNull)
            val canRemove = attendanceRequestRepository.containsByOpenCampusDates(
                survey.openCampusSchedule,
            ).not()
            if (!canRemove) return Err(RemoveAttendanceSurveyUseCaseException.UnsupportedOperation)
            return Ok(attendanceSurveyRepository.remove(attendanceSurveyId))
        }
}

sealed class RemoveAttendanceSurveyUseCaseException : Exception() {
    object UnsupportedOperation : RemoveAttendanceSurveyUseCaseException()
}

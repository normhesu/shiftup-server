package app.vercel.shiftup.features.attendance.request.application

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.request.domain.model.AttendanceRequest
import app.vercel.shiftup.features.attendance.request.infra.AttendanceRequestRepository
import app.vercel.shiftup.features.user.account.domain.model.Cast
import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.account.infra.UserRepository
import org.koin.core.annotation.Single

@Single
class ApplyAttendanceRequestToOpenCampusDateUseCase(
    private val userRepository: UserRepository,
    private val attendanceRequestRepository: AttendanceRequestRepository,
) {
    suspend operator fun invoke(openCampusDate: OpenCampusDate, userIds: Set<UserId>) {
        require(openCampusDate >= OpenCampusDate.now())

        val castIds = userRepository.findAvailableUserByIds(userIds).map { Cast(it).id }
        val currentRequests = attendanceRequestRepository.findByOpenCampusDate(openCampusDate).toSet()
        val applyRequests = castIds.map {
            AttendanceRequest(castId = it, openCampusDate = openCampusDate)
        }.toSet()

        attendanceRequestRepository.addAndRemoveAll(
            addAttendanceRequests = applyRequests - currentRequests,
            removeAttendanceRequests = currentRequests - applyRequests,
        )
    }
}

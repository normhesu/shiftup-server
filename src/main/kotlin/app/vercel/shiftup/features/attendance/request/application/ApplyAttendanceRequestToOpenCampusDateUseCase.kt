package app.vercel.shiftup.features.attendance.request.application

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.request.domain.model.AttendanceRequest
import app.vercel.shiftup.features.attendance.request.infra.AttendanceRequestRepository
import app.vercel.shiftup.features.user.account.domain.model.Cast
import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.account.infra.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.koin.core.annotation.Single

@Single
class ApplyAttendanceRequestToOpenCampusDateUseCase(
    private val userRepository: UserRepository,
    private val attendanceRequestRepository: AttendanceRequestRepository,
) {
    suspend operator fun invoke(openCampusDate: OpenCampusDate, userIds: Set<UserId>) = coroutineScope {
        require(openCampusDate >= OpenCampusDate.now())

        val castIdsDeferred = async { userRepository.findAvailableUserByIds(userIds).map { Cast(it).id } }
        val currentRequestsDeferred = async { attendanceRequestRepository.findByOpenCampusDate(openCampusDate).toSet() }

        val applyRequests = castIdsDeferred.await().map {
            AttendanceRequest(castId = it, openCampusDate = openCampusDate)
        }.toSet()
        val currentRequests = currentRequestsDeferred.await()

        attendanceRequestRepository.addAndRemoveAll(
            addAttendanceRequests = applyRequests - currentRequests,
            removeAttendanceRequests = currentRequests - applyRequests,
        )
    }
}

package app.vercel.shiftup.features.attendance.request.application

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.request.domain.model.AttendanceRequest
import app.vercel.shiftup.features.attendance.request.domain.model.value.AttendanceRequestState
import app.vercel.shiftup.features.attendance.request.infra.AttendanceRequestRepository
import app.vercel.shiftup.features.user.account.domain.model.Cast
import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.account.infra.UserRepository
import org.koin.core.annotation.Single

@Single
class GetRespondableAttendanceRequestUseCase(
    private val userRepository: UserRepository,
    private val attendanceRequestRepository: AttendanceRequestRepository,
) {
    suspend operator fun invoke(userId: UserId): List<AttendanceRequest> {
        val castId = userRepository.findAvailableUserById(userId)
            .let(::checkNotNull)
            .let { Cast(it).id }
        return attendanceRequestRepository.findByCastIdAndStateAndEarliestDate(
            castId = castId,
            state = AttendanceRequestState.Blank,
            earliestDate = OpenCampusDate.now(),
        )
    }
}

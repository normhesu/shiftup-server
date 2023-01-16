package app.vercel.shiftup.features.attendance.request.application

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.request.domain.model.value.AttendanceRequestState
import app.vercel.shiftup.features.attendance.request.infra.AttendanceRequestRepository
import app.vercel.shiftup.features.user.account.domain.model.Cast
import app.vercel.shiftup.features.user.account.infra.UserRepository
import org.koin.core.annotation.Single

@Single
class GetCastsByAttendanceRequestUseCase(
    private val attendanceRequestRepository: AttendanceRequestRepository,
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(
        openCampusDate: OpenCampusDate,
        state: AttendanceRequestState,
    ): List<Cast> {
        val requests = attendanceRequestRepository.findByOpenCampusDateAndState(
            openCampusDate = openCampusDate,
            state = state,
        )
        return userRepository.findCastByCastIds(
            requests.map { it.castId },
        )
    }
}

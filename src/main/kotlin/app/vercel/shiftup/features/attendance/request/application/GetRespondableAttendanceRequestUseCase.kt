package app.vercel.shiftup.features.attendance.request.application

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.request.domain.model.value.AttendanceRequestState
import app.vercel.shiftup.features.attendance.request.infra.AttendanceRequestRepository
import app.vercel.shiftup.features.user.account.application.service.GetCastApplicationService
import app.vercel.shiftup.features.user.account.domain.model.UserId
import org.koin.core.annotation.Single

@Single
class GetRespondableAttendanceRequestUseCase(
    private val attendanceRequestRepository: AttendanceRequestRepository,
    private val getCastApplicationService: GetCastApplicationService,
) {
    suspend operator fun invoke(
        userId: UserId,
    ) = attendanceRequestRepository.findByCastIdAndStateAndEarliestDate(
        castId = getCastApplicationService(userId).id,
        state = AttendanceRequestState.Blank,
        earliestDate = OpenCampusDate.now(),
    )
}

package app.vercel.shiftup.features.attendance.request.application

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.request.infra.AttendanceRequestRepository
import app.vercel.shiftup.features.user.account.application.service.GetCastApplicationService
import app.vercel.shiftup.features.user.account.domain.model.UserId
import org.koin.core.annotation.Single

@Single
class GetAfterNowAttendanceRequestUseCase(
    private val attendanceRequestRepository: AttendanceRequestRepository,
    private val getCastApplicationService: GetCastApplicationService,
) {
    suspend operator fun invoke(
        userId: UserId,
    ) = attendanceRequestRepository.findByCastIdAndEarliestDate(
        castId = getCastApplicationService(userId).id,
        earliestDate = OpenCampusDate.now(),
    ).sortedBy { it.openCampusDate }
}

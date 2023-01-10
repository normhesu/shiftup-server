package app.vercel.shiftup.features.attendance.request.application

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.request.domain.model.AttendanceRequest
import app.vercel.shiftup.features.attendance.request.infra.AttendanceRequestRepository
import app.vercel.shiftup.features.user.account.application.service.GetCastApplicationService
import app.vercel.shiftup.features.user.account.domain.model.UserId
import org.koin.core.annotation.Single

@Single
class GetAfterNowDividedByRespondAttendanceRequestUseCase(
    private val attendanceRequestRepository: AttendanceRequestRepository,
    private val getCastApplicationService: GetCastApplicationService,
) {
    suspend operator fun invoke(
        userId: UserId,
    ): GetAfterNowDividedByRespondAttendanceRequestUseCaseResult {
        val requests = attendanceRequestRepository.findByCastIdAndEarliestDate(
            castId = getCastApplicationService(userId).id,
            earliestDate = OpenCampusDate.now(),
        ).sortedBy { it.openCampusDate }

        val (canRespondRequests, respondedRequests) = requests.partition {
            it.canRespond
        }
        return GetAfterNowDividedByRespondAttendanceRequestUseCaseResult(
            canRespondRequests = canRespondRequests,
            respondedRequests = respondedRequests
        )
    }
}

data class GetAfterNowDividedByRespondAttendanceRequestUseCaseResult(
    val canRespondRequests: List<AttendanceRequest>,
    val respondedRequests: List<AttendanceRequest>,
)

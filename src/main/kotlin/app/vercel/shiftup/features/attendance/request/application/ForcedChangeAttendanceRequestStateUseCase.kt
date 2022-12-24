package app.vercel.shiftup.features.attendance.request.application

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.request.domain.model.AttendanceRequestId
import app.vercel.shiftup.features.attendance.request.domain.model.value.AttendanceRequestState
import app.vercel.shiftup.features.attendance.request.infra.AttendanceRequestRepository
import app.vercel.shiftup.features.user.account.application.service.GetCastApplicationService
import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.account.infra.UserRepository
import io.ktor.server.plugins.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.koin.core.annotation.Single

@Single
class ForcedChangeAttendanceRequestStateUseCase(
    private val attendanceRequestRepository: AttendanceRequestRepository,
    private val userRepository: UserRepository,
    private val getCastApplicationService: GetCastApplicationService,
) {
    suspend operator fun invoke(
        userId: UserId,
        openCampusDate: OpenCampusDate,
        state: AttendanceRequestState,
        operatorId: UserId,
    ) = coroutineScope {
        val castIdDeferred = async {
            getCastApplicationService(userId).id
        }

        val operatorDeferred = async {
            userRepository.findAvailableUserById(operatorId)
                .let(::checkNotNull)
        }

        val request = attendanceRequestRepository.findById(
            AttendanceRequestId(
                castId = castIdDeferred.await(),
                openCampusDate = openCampusDate,
            )
        ) ?: throw NotFoundException()
        val newRequest = request.forcedChangeState(state, operatorDeferred.await())

        attendanceRequestRepository.replace(newRequest)
    }
}

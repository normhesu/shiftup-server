package app.vercel.shiftup.features.attendance.request.application

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.request.domain.model.AttendanceRequestId
import app.vercel.shiftup.features.attendance.request.domain.model.value.AttendanceRequestState
import app.vercel.shiftup.features.attendance.request.infra.AttendanceRequestRepository
import app.vercel.shiftup.features.user.account.application.service.GetCastApplicationService
import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.account.infra.UserRepository
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
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
    ): Result<Unit, ForcedChangeAttendanceRequestStateUseCaseException> = coroutineScope {
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
        ) ?: return@coroutineScope Err(
            ForcedChangeAttendanceRequestStateUseCaseException.NotFoundRequest,
        )
        val newRequest = request.forcedChangeState(state, operatorDeferred.await())

        attendanceRequestRepository.replace(newRequest)
        Ok(Unit)
    }
}

sealed class ForcedChangeAttendanceRequestStateUseCaseException : Exception() {
    object NotFoundRequest : ForcedChangeAttendanceRequestStateUseCaseException()
}

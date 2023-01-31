package app.vercel.shiftup.features.attendance.request.application

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.request.domain.model.AttendanceRequestId
import app.vercel.shiftup.features.attendance.request.domain.model.value.AttendanceRequestState
import app.vercel.shiftup.features.attendance.request.infra.AttendanceRequestRepository
import app.vercel.shiftup.features.user.account.application.service.GetCastApplicationService
import app.vercel.shiftup.features.user.account.domain.model.CastId
import app.vercel.shiftup.features.user.account.domain.model.UserId
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.koin.core.annotation.Single

@Single
class RespondAttendanceRequestUseCase(
    private val attendanceRequestRepository: AttendanceRequestRepository,
    private val getCastApplicationService: GetCastApplicationService,
) {
    suspend operator fun invoke(
        userId: UserId,
        openCampusDate: OpenCampusDate,
        state: AttendanceRequestState.NonBlank,
    ): Result<Unit, RespondAttendanceRequestUseCaseException> = coroutineScope {
        val castDeferred = async { getCastApplicationService(userId) }
        val request = attendanceRequestRepository.findById(
            AttendanceRequestId(
                castId = CastId.unsafe(userId),
                openCampusDate = openCampusDate,
            )
        ) ?: return@coroutineScope Err(
            RespondAttendanceRequestUseCaseException.NotFoundRequest,
        )

        val newRequest = request.respond(state).getOrElse {
            return@coroutineScope Err(
                RespondAttendanceRequestUseCaseException.Responded,
            )
        }
        castDeferred.await()
        attendanceRequestRepository.replace(newRequest)

        Ok(Unit)
    }
}

sealed class RespondAttendanceRequestUseCaseException : Exception() {
    object NotFoundRequest : RespondAttendanceRequestUseCaseException()
    object Responded : RespondAttendanceRequestUseCaseException()
}

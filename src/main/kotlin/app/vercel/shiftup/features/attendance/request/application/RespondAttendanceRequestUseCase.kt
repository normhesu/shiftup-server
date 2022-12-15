package app.vercel.shiftup.features.attendance.request.application

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.request.domain.model.AttendanceRequestId
import app.vercel.shiftup.features.attendance.request.domain.model.value.AttendanceRequestState
import app.vercel.shiftup.features.attendance.request.infra.AttendanceRequestRepository
import app.vercel.shiftup.features.user.account.domain.model.Cast
import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.account.infra.UserRepository
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import io.ktor.server.plugins.*
import org.koin.core.annotation.Single

@Single
class RespondAttendanceRequestUseCase(
    private val userRepository: UserRepository,
    private val attendanceRequestRepository: AttendanceRequestRepository,
) {
    suspend operator fun invoke(
        userId: UserId,
        openCampusDate: OpenCampusDate,
        state: AttendanceRequestState.NonBlank,
    ): Result<Unit, IllegalStateException> {
        val castId = userRepository.findAvailableUserById(userId)
            .let(::checkNotNull)
            .let { Cast(it).id }

        val request = attendanceRequestRepository.findById(
            AttendanceRequestId(
                castId = castId,
                openCampusDate = openCampusDate,
            )
        ) ?: throw NotFoundException()
        val newRequest = request.respond(state).getOrElse {
            if (it !is IllegalStateException) throw it
            return Err(it)
        }

        attendanceRequestRepository.replace(newRequest)
        return Ok(Unit)
    }
}

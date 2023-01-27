package app.vercel.shiftup.features.user.account.application

import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.account.infra.AvailableUserRepository
import app.vercel.shiftup.features.user.invite.domain.model.value.FirstManager
import app.vercel.shiftup.features.user.invite.domain.model.value.Position
import app.vercel.shiftup.features.user.invite.infra.InviteRepository
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.fold
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.koin.core.annotation.Single

@Single
class ChangeUserPositionUseCase(
    private val availableUserRepository: AvailableUserRepository,
    private val inviteRepository: InviteRepository,
    private val firstManager: FirstManager,
) {
    suspend operator fun invoke(
        userId: UserId,
        position: Position,
        operatorId: UserId,
    ): Result<Unit, ChangeUserPositionUseCaseException> = coroutineScope {
        val userDeferred = async { availableUserRepository.findById(userId) }
        val operatorDeferred = async {
            availableUserRepository.findById(operatorId)
                .let(::requireNotNull)
        }

        val user = userDeferred.await() ?: return@coroutineScope Err(
            ChangeUserPositionUseCaseException.UserNotFound,
        )
        if (user.studentNumber == firstManager.studentNumber) {
            return@coroutineScope Err(
                ChangeUserPositionUseCaseException.UnsupportedOperation()
            )
        }

        val invite = inviteRepository.findByStudentNumber(user.studentNumber)
            .let(::checkNotNull)

        invite.changePosition(
            position = position,
            operator = operatorDeferred.await(),
        ).fold(
            success = {
                Ok(inviteRepository.replace(it))
            },
            failure = {
                Err(ChangeUserPositionUseCaseException.UnsupportedOperation(it))
            }
        )
    }
}

sealed class ChangeUserPositionUseCaseException(cause: Throwable? = null) : Exception(cause) {
    object UserNotFound : ChangeUserPositionUseCaseException()
    class UnsupportedOperation(cause: Throwable? = null) : ChangeUserPositionUseCaseException(cause)
}

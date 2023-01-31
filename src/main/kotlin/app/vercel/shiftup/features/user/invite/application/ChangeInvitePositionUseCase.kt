package app.vercel.shiftup.features.user.invite.application

import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.account.infra.AvailableUserRepository
import app.vercel.shiftup.features.user.invite.domain.model.InviteId
import app.vercel.shiftup.features.user.invite.domain.model.value.Position
import app.vercel.shiftup.features.user.invite.infra.InviteRepository
import com.github.michaelbull.result.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.koin.core.annotation.Single

@Single
class ChangeInvitePositionUseCase(
    private val availableUserRepository: AvailableUserRepository,
    private val inviteRepository: InviteRepository,
) {
    suspend operator fun invoke(
        inviteId: InviteId,
        position: Position,
        operatorId: UserId,
    ): Result<Unit, ChangeInvitePositionUseCaseException> = coroutineScope {
        val inviteDeferred = async {
            inviteRepository.findById(inviteId)
                .let(::checkNotNull)
        }
        val operatorDeferred = async {
            availableUserRepository.findById(operatorId)
                .let(::requireNotNull)
        }

        inviteDeferred.await().changePosition(
            position = position,
            operator = operatorDeferred.await(),
        ).onSuccess {
            inviteRepository.replace(it)
        }.fold(
            success = { Ok(Unit) },
            failure = {
                Err(ChangeInvitePositionUseCaseException.UnsupportedOperation(it))
            }
        )
    }
}

sealed class ChangeInvitePositionUseCaseException(cause: Throwable? = null) : Exception(cause) {
    class UnsupportedOperation(cause: Throwable? = null) : ChangeInvitePositionUseCaseException(cause)
}

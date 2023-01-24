package app.vercel.shiftup.features.user.invite.application

import app.vercel.shiftup.features.user.invite.domain.model.Invite
import app.vercel.shiftup.features.user.invite.domain.model.value.FirstManager
import app.vercel.shiftup.features.user.invite.domain.model.value.Position
import app.vercel.shiftup.features.user.invite.infra.InviteRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.runCatching
import com.github.michaelbull.result.throwIf
import com.mongodb.MongoException
import org.koin.core.annotation.Single

@Single
class AddInviteUseCase(
    private val inviteRepository: InviteRepository,
    private val firstManager: FirstManager,
) {
    companion object {
        private const val DUPLICATE_KEY_CODE = 11000
    }

    suspend operator fun invoke(invite: Invite): Result<Unit, AddInviteUseCaseException> {
        if (invite.studentNumber == firstManager.studentNumber) {
            require(invite.position == Position.Manager)
        }

        return runCatching {
            inviteRepository.add(invite)
        }.throwIf {
            (it is MongoException && it.code == DUPLICATE_KEY_CODE).not()
        }.mapError {
            AddInviteUseCaseException.Invited(it)
        }
    }
}

sealed class AddInviteUseCaseException(override val cause: Throwable) : Exception(cause) {
    class Invited(override val cause: Throwable) : AddInviteUseCaseException(cause)
}

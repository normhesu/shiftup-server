package app.vercel.shiftup.features.user.invite.application

import app.vercel.shiftup.features.user.invite.domain.model.Invite
import app.vercel.shiftup.features.user.invite.domain.model.value.FirstManager
import app.vercel.shiftup.features.user.invite.domain.model.value.Position
import app.vercel.shiftup.features.user.invite.infra.InviteRepository
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import org.koin.core.annotation.Single

@Single
class AddInviteUseCase(
    private val inviteRepository: InviteRepository,
    private val firstManager: FirstManager,
) {
    suspend operator fun invoke(invite: Invite): Result<Unit, AddInviteUseCaseException> {
        if (invite.studentNumber == firstManager.studentNumber) {
            require(invite.position == Position.Manager)
        }

        val invited = inviteRepository.addOrNothingAndGetAddedResult(invite)
        return if (!invited) Ok(Unit) else Err(AddInviteUseCaseException.Invited)
    }
}

sealed class AddInviteUseCaseException : Exception() {
    object Invited : AddInviteUseCaseException()
}

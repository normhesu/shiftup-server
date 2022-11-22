package app.vercel.shiftup.features.user.invite.application

import app.vercel.shiftup.features.user.invite.domain.model.Invite
import app.vercel.shiftup.features.user.invite.infra.InviteRepository
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import org.koin.core.annotation.Single

@Single
class AddInviteUseCase(
    private val inviteRepository: InviteRepository,
) {
    suspend operator fun invoke(invite: Invite) = runSuspendCatching<Unit> {
        val invited = inviteRepository.findByStudentNumber(
            invite.studentNumber,
        ) != null
        if (invited) throw InvitedException()
        inviteRepository.add(invite)
    }.mapError {
        if (it !is InvitedException) throw it
        it
    }
}

class InvitedException : Exception()

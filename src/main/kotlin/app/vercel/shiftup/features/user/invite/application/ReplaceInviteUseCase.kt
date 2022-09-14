package app.vercel.shiftup.features.user.invite.application

import app.vercel.shiftup.features.user.invite.domain.model.Invite
import app.vercel.shiftup.features.user.invite.infra.InviteRepository
import org.koin.core.annotation.Single

@Single
class ReplaceInviteUseCase(
    private val inviteRepository: InviteRepository,
) {
    suspend operator fun invoke(invite: Invite) {
        inviteRepository.replace(invite)
    }
}

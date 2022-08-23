package app.vercel.shiftup.features.user.invite.application

import app.vercel.shiftup.features.user.invite.domain.model.Invite
import app.vercel.shiftup.features.user.invite.infra.InviteRepository
import org.koin.core.annotation.Single

@Single
class AddInviteUseCase(
    private val inviteRepository: InviteRepository,
) {
    suspend operator fun invoke(invite: Invite) {
        val invited = inviteRepository.findByStudentNumber(
            invite.studentNumber,
        ) != null
        require(!invited) { "既に招待されています" }
        inviteRepository.add(invite)
    }
}

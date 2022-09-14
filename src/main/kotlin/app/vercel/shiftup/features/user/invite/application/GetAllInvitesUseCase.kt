package app.vercel.shiftup.features.user.invite.application

import app.vercel.shiftup.features.user.invite.infra.InviteRepository
import org.koin.core.annotation.Single

@Single
class GetAllInvitesUseCase(
    private val inviteRepository: InviteRepository,
) {
    suspend operator fun invoke() = inviteRepository.findAll()
}

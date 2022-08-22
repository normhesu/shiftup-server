package app.vercel.shiftup.features.user.invited.application

import app.vercel.shiftup.features.user.invited.infra.InvitedUserRepository
import org.koin.core.annotation.Single

@Single
class GetAllInvitedUsersUseCase(
    private val invitedUserRepository: InvitedUserRepository,
) {
    suspend operator fun invoke() = invitedUserRepository.findAll()
}

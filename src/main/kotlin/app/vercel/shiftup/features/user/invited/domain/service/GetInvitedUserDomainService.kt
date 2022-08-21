package app.vercel.shiftup.features.user.invited.domain.service

import app.vercel.shiftup.features.user.domain.model.value.NeecEmail
import app.vercel.shiftup.features.user.invited.domain.model.InvitedUser
import app.vercel.shiftup.features.user.invited.domain.model.value.FirstManager
import org.koin.core.annotation.Single

@Single
class GetInvitedUserDomainService(
    private val invitedUserRepository: InvitedUserRepositoryInterface,
) {
    suspend operator fun invoke(
        email: NeecEmail,
        firstManager: FirstManager,
    ): InvitedUser? {
        return invitedUserRepository.findByEmail(email)
            ?: InvitedUser.firstInvitedManager(
                email = email,
                firstManager = firstManager,
            )
    }
}

package app.vercel.shiftup.features.user.invite.domain.service

import app.vercel.shiftup.features.user.domain.model.value.Email
import app.vercel.shiftup.features.user.invite.domain.model.Invite
import app.vercel.shiftup.features.user.invite.domain.model.value.FirstManager
import org.koin.core.annotation.Single

@Single
class GetInviteDomainService(
    private val inviteRepository: InviteRepositoryInterface,
) {
    suspend operator fun invoke(
        email: Email,
        firstManager: FirstManager,
    ): Invite? {
        return inviteRepository.findByEmail(email)
            ?: Invite(
                email = email,
                firstManager = firstManager,
            )
    }
}

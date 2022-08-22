package app.vercel.shiftup.features.user.invited.application

import app.vercel.shiftup.features.user.invited.domain.model.InvitedUser
import app.vercel.shiftup.features.user.invited.infra.InvitedUserRepository
import com.mongodb.client.result.DeleteResult
import org.koin.core.annotation.Single

@Single
class RemoveInvitedUsersUseCase(
    private val invitedUserRepository: InvitedUserRepository,
) {
    suspend operator fun invoke(
        invitedUser: InvitedUser,
    ): DeleteResult {
        return invitedUserRepository.remove(invitedUser.id)
    }
}

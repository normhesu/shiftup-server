package app.vercel.shiftup.features.user.invite.application

import app.vercel.shiftup.features.user.invite.domain.model.InviteId
import app.vercel.shiftup.features.user.invite.infra.InviteRepository
import com.mongodb.client.result.DeleteResult
import org.koin.core.annotation.Single

@Single
class RemoveInviteUseCase(
    private val inviteRepository: InviteRepository,
) {
    suspend operator fun invoke(inviteId: InviteId): DeleteResult {
        return inviteRepository.remove(inviteId)
    }
}

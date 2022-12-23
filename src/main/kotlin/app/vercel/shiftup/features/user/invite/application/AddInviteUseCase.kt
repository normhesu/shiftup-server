package app.vercel.shiftup.features.user.invite.application

import app.vercel.shiftup.features.user.invite.domain.model.Invite
import app.vercel.shiftup.features.user.invite.infra.InviteRepository
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import com.mongodb.MongoException
import org.koin.core.annotation.Single

@Single
class AddInviteUseCase(
    private val inviteRepository: InviteRepository,
) {
    companion object {
        private const val DUPLICATE_KEY_CODE = 11000
    }

    suspend operator fun invoke(invite: Invite) = runSuspendCatching<Unit> {
        inviteRepository.add(invite)
    }.mapError {
        val invited = it is MongoException && it.code == DUPLICATE_KEY_CODE
        if (invited) InvitedException() else throw it
    }
}

class InvitedException : Exception()

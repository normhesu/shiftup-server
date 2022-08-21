package app.vercel.shiftup.features.user.invited.infra

import app.vercel.shiftup.features.user.domain.model.value.NeecEmail
import app.vercel.shiftup.features.user.invited.domain.model.InvitedUser
import app.vercel.shiftup.features.user.invited.domain.service.InvitedUserRepositoryInterface
import org.koin.core.annotation.Single
import org.litote.kmongo.coroutine.CoroutineDatabase

@Single(binds = [InvitedUserRepositoryInterface::class])
class InvitedUserRepository(
    private val database: CoroutineDatabase
) : InvitedUserRepositoryInterface {
    private val collection get() = database.getCollection<InvitedUser>()

    override suspend fun findByEmail(
        email: NeecEmail,
    ): InvitedUser? {
        return collection.findOne(InvitedUser::studentNumber eq email.studentNumber)
    }
}

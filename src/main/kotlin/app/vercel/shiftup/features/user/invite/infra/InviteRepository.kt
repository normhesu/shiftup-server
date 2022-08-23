package app.vercel.shiftup.features.user.invite.infra

import app.vercel.shiftup.features.core.infra.orThrow
import app.vercel.shiftup.features.user.domain.model.value.NeecEmail
import app.vercel.shiftup.features.user.domain.model.value.StudentNumber
import app.vercel.shiftup.features.user.invite.domain.model.Invite
import app.vercel.shiftup.features.user.invite.domain.model.InviteId
import app.vercel.shiftup.features.user.invite.domain.service.InviteRepositoryInterface
import com.mongodb.client.result.DeleteResult
import org.koin.core.annotation.Single
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.toList
import org.litote.kmongo.coroutine.updateOne
import org.litote.kmongo.eq

@Single(binds = [InviteRepositoryInterface::class])
class InviteRepository(
    private val database: CoroutineDatabase
) : InviteRepositoryInterface {
    private val collection get() = database.getCollection<Invite>()

    override suspend fun findByEmail(
        email: NeecEmail,
    ): Invite? {
        return findByStudentNumber(email.studentNumber)
    }

    suspend fun findByStudentNumber(
        studentNumber: StudentNumber,
    ): Invite? {
        return collection.findOne(Invite::studentNumber eq studentNumber)
    }

    suspend fun findAll(): List<Invite> {
        return collection.find().publisher.toList()
    }

    suspend fun add(invite: Invite) {
        collection.insertOne(invite).orThrow()
    }

    suspend fun replace(invite: Invite) {
        collection.updateOne(invite).orThrow()
    }

    suspend fun remove(inviteId: InviteId): DeleteResult {
        return collection.deleteOneById(inviteId).orThrow()
    }
}

package app.vercel.shiftup.features.user.invited.infra

import app.vercel.shiftup.features.core.infra.orThrow
import app.vercel.shiftup.features.user.domain.model.value.NeecEmail
import app.vercel.shiftup.features.user.domain.model.value.StudentNumber
import app.vercel.shiftup.features.user.invited.domain.model.InvitedUser
import app.vercel.shiftup.features.user.invited.domain.model.InvitedUserId
import app.vercel.shiftup.features.user.invited.domain.service.InvitedUserRepositoryInterface
import com.mongodb.client.result.DeleteResult
import org.koin.core.annotation.Single
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.toList
import org.litote.kmongo.coroutine.updateOne
import org.litote.kmongo.eq

@Single(binds = [InvitedUserRepositoryInterface::class])
class InvitedUserRepository(
    private val database: CoroutineDatabase
) : InvitedUserRepositoryInterface {
    private val collection get() = database.getCollection<InvitedUser>()

    override suspend fun findByEmail(
        email: NeecEmail,
    ): InvitedUser? {
        return findByStudentNumber(email.studentNumber)
    }

    suspend fun findByStudentNumber(
        studentNumber: StudentNumber,
    ): InvitedUser? {
        return collection.findOne(InvitedUser::studentNumber eq studentNumber)
    }

    suspend fun findAll(): List<InvitedUser> {
        return collection.find().publisher.toList()
    }

    suspend fun add(invitedUser: InvitedUser) {
        collection.insertOne(invitedUser).orThrow()
    }

    suspend fun replace(invitedUser: InvitedUser) {
        collection.updateOne(invitedUser).orThrow()
    }

    suspend fun remove(invitedUserId: InvitedUserId): DeleteResult {
        return collection.deleteOneById(invitedUserId).orThrow()
    }
}

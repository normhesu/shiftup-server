package app.vercel.shiftup.features.user.invite.infra

import app.vercel.shiftup.features.core.infra.orThrow
import app.vercel.shiftup.features.user.domain.model.value.Email
import app.vercel.shiftup.features.user.domain.model.value.StudentNumber
import app.vercel.shiftup.features.user.invite.domain.model.Invite
import app.vercel.shiftup.features.user.invite.domain.model.InviteId
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapBoth
import com.github.michaelbull.result.throwIf
import com.mongodb.MongoException
import com.mongodb.client.result.DeleteResult
import org.koin.core.annotation.Single
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.updateOne
import org.litote.kmongo.upsert

@Single
class InviteRepository(
    private val database: CoroutineDatabase,
) {
    companion object {
        private const val DUPLICATE_KEY_CODE = 11000
    }

    private val collection get() = database.getCollection<Invite>()

    suspend fun findByEmail(
        email: Email,
    ): Invite? {
        return findByStudentNumber(email.studentNumber)
    }

    suspend fun findByStudentNumber(
        studentNumber: StudentNumber,
    ): Invite? {
        return collection.findOneById(InviteId(studentNumber))
    }

    suspend fun findAll(): List<Invite> {
        return collection.find().toList()
    }

    suspend fun addOrNothingAndGetContainsBeforeAdd(invite: Invite) = runSuspendCatching {
        collection.insertOne(invite).orThrow()
    }.throwIf {
        (it is MongoException && it.code == DUPLICATE_KEY_CODE).not()
    }.mapBoth(
        success = { false },
        failure = { true },
    )

    suspend fun replace(invite: Invite) {
        collection.updateOne(invite).orThrow()
    }

    suspend fun addOrReplace(invite: Invite) {
        collection.updateOne(invite, upsert()).orThrow()
    }

    suspend fun remove(inviteId: InviteId): DeleteResult {
        return collection.deleteOneById(inviteId).orThrow()
    }
}

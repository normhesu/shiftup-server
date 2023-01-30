package app.vercel.shiftup.features.user.account.infra

import app.vercel.shiftup.features.core.infra.orThrow
import app.vercel.shiftup.features.core.infra.throwIfNotDuplicate
import app.vercel.shiftup.features.user.account.domain.model.User
import app.vercel.shiftup.features.user.account.domain.model.UserId
import com.github.michaelbull.result.coroutines.runSuspendCatching
import org.koin.core.annotation.Single
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.updateOne
import org.litote.kmongo.eq

@Single
class UserRepository(
    database: CoroutineDatabase,
) {
    private val collection = database.getCollection<User>()

    suspend fun findById(userId: UserId): User? {
        return collection.findOneById(userId)
    }

    suspend fun addOrNothing(user: User) {
        runSuspendCatching {
            collection.insertOne(user).orThrow()
        }.throwIfNotDuplicate()
    }

    suspend fun replace(user: User) {
        collection.updateOne(user).orThrow()
    }

    suspend fun contains(id: UserId) = collection.countDocuments(User::id eq id) != 0L
}

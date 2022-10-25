package app.vercel.shiftup.features.user.account.infra

import app.vercel.shiftup.features.core.infra.orThrow
import app.vercel.shiftup.features.user.account.domain.model.User
import app.vercel.shiftup.features.user.account.domain.model.UserId
import org.koin.core.annotation.Single
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.`in`

@Single
class UserRepository(
    private val database: CoroutineDatabase,
) {
    private val collection get() = database.getCollection<User>()

    suspend fun add(user: User) {
        collection.insertOne(user).orThrow()
    }

    suspend fun findById(id: UserId): User? {
        return collection.findOneById(id)
    }

    suspend fun findByIds(ids: Iterable<UserId>): List<User> {
        return collection.find(User::id `in` ids).toList()
    }
}

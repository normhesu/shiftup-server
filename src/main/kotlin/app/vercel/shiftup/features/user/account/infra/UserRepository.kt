package app.vercel.shiftup.features.user.account.infra

import app.vercel.shiftup.features.core.infra.orThrow
import app.vercel.shiftup.features.user.account.domain.model.User
import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.domain.model.value.Email
import app.vercel.shiftup.features.user.domain.model.value.SchoolProfile
import app.vercel.shiftup.features.user.domain.model.value.StudentNumber
import com.mongodb.client.model.Filters
import org.bson.conversions.Bson
import org.koin.core.annotation.Single
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.`in`
import org.litote.kmongo.path
import org.litote.kmongo.rem
import kotlin.reflect.KProperty1

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

    suspend fun findByStudentNumbers(studentNumbers: Iterable<StudentNumber>): List<User> {
        val fieldName = User::schoolProfile % SchoolProfile::email
        val pattern = studentNumbers.joinToString(separator = "|") { it.lowercaseValue() }
        return collection.find(fieldName regex pattern).toList()
    }

    private infix fun KProperty1<User, Email?>.regex(regex: String): Bson = Filters.regex(path(), regex)
}

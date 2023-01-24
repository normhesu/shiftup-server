package app.vercel.shiftup.features.user.account.infra

import app.vercel.shiftup.features.core.infra.orThrow
import app.vercel.shiftup.features.user.account.domain.model.AvailableUser
import app.vercel.shiftup.features.user.account.domain.model.User
import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.domain.model.value.StudentNumber
import app.vercel.shiftup.features.user.invite.domain.model.Invite
import app.vercel.shiftup.features.user.invite.domain.model.InviteId
import app.vercel.shiftup.features.user.invite.domain.model.value.FirstManager
import com.mongodb.client.model.Filters
import org.koin.core.annotation.Single
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.updateOne
import org.litote.kmongo.`in`
import org.litote.kmongo.path

@Single
class UserRepository(
    private val database: CoroutineDatabase,
    firstManager: FirstManager,
) {
    private val firstManagerInvite = Invite(firstManager)
    private val userCollection get() = database.getCollection<User>()
    private val inviteCollection get() = database.getCollection<Invite>()

    suspend fun add(user: User) {
        userCollection.insertOne(user).orThrow()
    }

    suspend fun replace(user: User) {
        userCollection.updateOne(user).orThrow()
    }

    suspend fun findAvailableUserById(id: UserId): AvailableUser? {
        val user = userCollection.findOneById(id) ?: return null
        val invite = findInvite(user) ?: return null
        return user.toAvailableUser(invite)
    }

    suspend fun findAvailableUserByIds(ids: Iterable<UserId>): List<AvailableUser> {
        val users = userCollection.find(User::id `in` ids).toList()
        val invites = findInvites(users)
        return users.toAvailableUsers(invites)
    }

    suspend fun findAvailableUserByStudentNumbers(studentNumbers: Iterable<StudentNumber>): List<AvailableUser> {
        val users = run {
            val filter = Filters.regex(
                User::email.path(),
                studentNumbers.joinToString(separator = "|") { it.lowercaseValue() },
            )
            userCollection.find(filter).toList()
        }
        val invites = findInvites(users)
        return users.toAvailableUsers(invites)
    }

    suspend fun contains(id: UserId) = userCollection.findOneById(id) != null

    private suspend fun findInvite(user: User): Invite? {
        return inviteCollection.findOneById(
            InviteId(user.studentNumber),
        ) ?: firstManagerInvite.takeIf {
            it.studentNumber == user.email.studentNumber
        }
    }

    private suspend fun findInvites(users: List<User>): List<Invite> {
        val invites = inviteCollection.find(
            Invite::id `in` users.map { InviteId(it.studentNumber) },
        ).toList()

        return when {
            invites.any { it == firstManagerInvite } -> invites
            users.all { it.studentNumber != firstManagerInvite.studentNumber } -> invites
            else -> invites + firstManagerInvite
        }
    }

    private fun List<User>.toAvailableUsers(invites: List<Invite>): List<AvailableUser> {
        val usersWithStudentNumber = this.associateBy { it.studentNumber }
        val invitesWithStudentNumber = invites.associateBy { it.studentNumber }
        return (usersWithStudentNumber.keys intersect invitesWithStudentNumber.keys).mapNotNull {
            val invite = invitesWithStudentNumber[it] ?: return@mapNotNull null
            usersWithStudentNumber[it]?.toAvailableUser(invite)
        }
    }
}

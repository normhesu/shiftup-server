package app.vercel.shiftup.features.user.account.infra

import app.vercel.shiftup.features.core.infra.orThrow
import app.vercel.shiftup.features.user.account.domain.model.AvailableUser
import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.account.domain.model.value.Name
import app.vercel.shiftup.features.user.domain.model.value.Email
import app.vercel.shiftup.features.user.domain.model.value.SchoolProfile
import app.vercel.shiftup.features.user.domain.model.value.StudentNumber
import app.vercel.shiftup.features.user.invite.domain.model.Invite
import app.vercel.shiftup.features.user.invite.domain.model.InviteId
import app.vercel.shiftup.features.user.invite.domain.model.value.FirstManager
import com.mongodb.client.model.Filters
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.`in`
import org.litote.kmongo.path

@Single
class UserRepository(
    private val database: CoroutineDatabase,
    firstManager: FirstManager,
) {
    private val firstManagerInvite = Invite(firstManager)
    private val userDTOCollection get() = database.getCollection<UserDTO>()
    private val inviteCollection get() = database.getCollection<Invite>()

    suspend fun add(availableUser: AvailableUser) {
        userDTOCollection.insertOne(availableUser.toDTO()).orThrow()
    }

    suspend fun findAvailableUserById(id: UserId): AvailableUser? {
        val userDTO = userDTOCollection.findOneById(id) ?: return null
        val invite = findInvite(userDTO) ?: return null
        return userDTO.toAvailableUser(invite)
    }

    suspend fun findAvailableUserByIds(ids: Iterable<UserId>): List<AvailableUser> {
        val userDTOs = userDTOCollection.find(UserDTO::id `in` ids).toList()
        val invites = findInvites(userDTOs)
        return userDTOs.toAvailableUsers(invites)
    }

    suspend fun findAvailableUserByStudentNumbers(studentNumbers: Iterable<StudentNumber>): List<AvailableUser> {
        val userDTOs = run {
            val filter = Filters.regex(
                UserDTO::email.path(),
                studentNumbers.joinToString(separator = "|") { it.lowercaseValue() },
            )
            userDTOCollection.find(filter).toList()
        }
        val invites = findInvites(userDTOs)
        return userDTOs.toAvailableUsers(invites)
    }

    suspend fun contains(id: UserId) = userDTOCollection.findOneById(id) != null

    private suspend fun findInvite(userDTO: UserDTO): Invite? {
        return inviteCollection.findOneById(
            InviteId(userDTO.studentNumber),
        ) ?: firstManagerInvite.takeIf {
            it.studentNumber == userDTO.email.studentNumber
        }
    }

    private suspend fun findInvites(userDTOs: List<UserDTO>): List<Invite> {
        val invites = inviteCollection.find(
            Invite::id `in` userDTOs.map { InviteId(it.studentNumber) },
        ).toList()

        return when {
            invites.any { it == firstManagerInvite } -> invites
            userDTOs.all { it.studentNumber != firstManagerInvite.studentNumber } -> invites
            else -> invites + firstManagerInvite
        }
    }

    private fun List<UserDTO>.toAvailableUsers(invites: List<Invite>): List<AvailableUser> {
        val userDTOsWithStudentNumber = this.associateBy { it.studentNumber }
        val invitesWithStudentNumber = invites.associateBy { it.studentNumber }
        return (userDTOsWithStudentNumber.keys intersect invitesWithStudentNumber.keys).mapNotNull {
            val invite = invitesWithStudentNumber[it] ?: return@mapNotNull null
            userDTOsWithStudentNumber[it]?.toAvailableUser(invite)
        }
    }
}

@Serializable
private data class UserDTO(
    @SerialName("_id") val id: UserId,
    val name: Name,
    val email: Email,
) {
    val studentNumber get() = email.studentNumber

    constructor(availableUser: AvailableUser) : this(
        id = availableUser.id,
        name = availableUser.name,
        email = availableUser.schoolProfile.email,
    )

    fun toAvailableUser(invite: Invite): AvailableUser {
        require(invite.studentNumber == studentNumber)
        return AvailableUser(
            id = id,
            name = name,
            position = invite.position,
            schoolProfile = SchoolProfile(
                department = invite.department,
                email = email,
            )
        )
    }
}

private fun AvailableUser.toDTO() = UserDTO(this)

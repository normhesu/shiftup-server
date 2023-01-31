package app.vercel.shiftup.features.user.account.domain.model

import app.vercel.shiftup.features.user.account.domain.model.value.Name
import app.vercel.shiftup.features.user.domain.model.value.Email
import app.vercel.shiftup.features.user.domain.model.value.SchoolProfile
import app.vercel.shiftup.features.user.invite.domain.model.Invite
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.*

@Serializable
@JvmInline
value class UserId(
    // セッションに保存が出来なくなるので、privateにしない
    val value: String,
)

@Serializable
data class User(
    @SerialName("_id") val id: UserId,
    val name: Name,
    val email: Email,
) {
    @Transient
    val studentNumber = email.studentNumber

    constructor(availableUser: AvailableUser) : this(
        id = availableUser.id,
        name = availableUser.name,
        email = availableUser.schoolProfile.email,
    )

    fun changeName(name: Name) = copy(name = name)

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

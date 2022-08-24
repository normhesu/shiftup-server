package app.vercel.shiftup.features.user.account.domain.model

import app.vercel.shiftup.features.user.account.domain.model.value.Name
import app.vercel.shiftup.features.user.domain.model.value.*
import app.vercel.shiftup.features.user.invite.domain.model.value.Position
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class UserId(@Suppress("unused") private val value: String)

@Serializable
data class User(
    @SerialName("_id") val id: UserId,
    val name: Name,
    val studentNumber: StudentNumber,
    val department: Department,
    val position: Position,
) {
    val roles: Set<Role>
        get() = position.roles

    val email: NeecEmail
        get() = NeecEmail.of(studentNumber)

    val schoolYear: SchoolYear?
        get() = studentNumber.getSchoolYear(
            tenure = department.tenure,
        )
}

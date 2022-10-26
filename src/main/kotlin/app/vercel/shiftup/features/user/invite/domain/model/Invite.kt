package app.vercel.shiftup.features.user.invite.domain.model

import app.vercel.shiftup.features.user.domain.model.value.Department
import app.vercel.shiftup.features.user.domain.model.value.NeecEmail
import app.vercel.shiftup.features.user.domain.model.value.StudentNumber
import app.vercel.shiftup.features.user.invite.domain.model.value.FirstManager
import app.vercel.shiftup.features.user.invite.domain.model.value.Position
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
@JvmInline
value class InviteId(
    @Suppress("unused")
    private val value: StudentNumber,
)

@Serializable
data class Invite(
    val studentNumber: StudentNumber,
    val department: Department,
    val position: Position,
    @SerialName("_id") val id: InviteId = InviteId(studentNumber),
) {
    companion object {
        operator fun invoke(
            email: NeecEmail,
            firstManager: FirstManager,
        ) = when (email) {
            NeecEmail.of(firstManager.studentNumber) -> Invite(
                position = Position.Manager,
                studentNumber = firstManager.studentNumber,
                department = firstManager.department,
            )
            else -> null
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Invite
        return id == other.id
    }

    override fun hashCode() = id.hashCode()
}

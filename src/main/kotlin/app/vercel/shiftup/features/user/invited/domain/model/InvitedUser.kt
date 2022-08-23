package app.vercel.shiftup.features.user.invited.domain.model

import app.vercel.shiftup.features.user.domain.model.value.Department
import app.vercel.shiftup.features.user.domain.model.value.NeecEmail
import app.vercel.shiftup.features.user.domain.model.value.StudentNumber
import app.vercel.shiftup.features.user.invited.domain.model.value.FirstManager
import app.vercel.shiftup.features.user.invited.domain.model.value.Position
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.litote.kmongo.Id
import org.litote.kmongo.newId

typealias InvitedUserId = Id<InvitedUser>

@Serializable
data class InvitedUser(
    val studentNumber: StudentNumber,
    val department: Department,
    val position: Position,
    @SerialName("_id") @Contextual val id: InvitedUserId = newId()
) {
    companion object {
        fun firstInvitedManager(
            email: NeecEmail,
            firstManager: FirstManager,
        ) = when (email) {
            NeecEmail.of(firstManager.studentNumber) -> InvitedUser(
                position = Position.Manager,
                studentNumber = firstManager.studentNumber,
                department = firstManager.department,
            )
            else -> null
        }
    }

    @Transient
    val roles = position.roles
}

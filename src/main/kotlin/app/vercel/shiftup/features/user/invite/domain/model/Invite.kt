package app.vercel.shiftup.features.user.invite.domain.model

import app.vercel.shiftup.features.user.domain.model.value.Department
import app.vercel.shiftup.features.user.domain.model.value.NeecEmail
import app.vercel.shiftup.features.user.domain.model.value.StudentNumber
import app.vercel.shiftup.features.user.invite.domain.model.value.FirstManager
import app.vercel.shiftup.features.user.invite.domain.model.value.Position
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId

typealias InviteId = Id<Invite>

@Serializable
data class Invite(
    val studentNumber: StudentNumber,
    val department: Department,
    val position: Position,
    @SerialName("_id") @Contextual val id: InviteId = newId()
) {
    companion object {
        fun fromFirstManager(
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
}

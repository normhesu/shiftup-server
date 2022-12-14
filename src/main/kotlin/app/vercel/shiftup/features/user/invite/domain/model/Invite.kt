package app.vercel.shiftup.features.user.invite.domain.model

import app.vercel.shiftup.features.user.domain.model.value.*
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
@Suppress("DataClassPrivateConstructor")
data class Invite private constructor(
    val studentNumber: StudentNumber,
    val department: Department,
    val position: Position,
    @SerialName("_id") val id: InviteId = InviteId(studentNumber),
) {

    init {
        val isNeec = studentNumber is NeecStudentNumber && department is NeecDepartment
        val isTeu = studentNumber is TeuStudentNumber && department is TeuDepartment
        require(isNeec || isTeu)
    }

    companion object {
        operator fun invoke(
            studentNumber: StudentNumber,
            department: Department,
            position: Position,
        ) = Invite(
            studentNumber = studentNumber,
            department = department,
            position = position,
        )

        operator fun invoke(
            firstManager: FirstManager,
        ) = Invite(
            position = Position.Manager,
            studentNumber = firstManager.studentNumber,
            department = firstManager.department,
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Invite
        return id == other.id
    }

    override fun hashCode() = id.hashCode()
}

package app.vercel.shiftup.features.user.account.domain.model

import app.vercel.shiftup.features.user.account.domain.model.value.Name
import app.vercel.shiftup.features.user.domain.model.value.*
import app.vercel.shiftup.features.user.invite.domain.model.value.Position
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val studentNumber: StudentNumber,
    val department: Department,
    val position: Position,
) {
    val roles: Set<Role>
        get() = position.roles

    val email: NeecEmail
        get() = NeecEmail.of(studentNumber)

    fun inSchool(fiscalYear: Int?) = getSchoolYear(fiscalYear) != null

    private fun getSchoolYear(fiscalYear: Int?) = studentNumber.getSchoolYear(
        tenure = department.tenure,
        fiscalYear = fiscalYear,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as User
        return id == other.id
    }

    override fun hashCode() = id.hashCode()
}

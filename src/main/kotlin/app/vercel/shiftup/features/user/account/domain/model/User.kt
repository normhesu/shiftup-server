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
@Suppress("DataClassPrivateConstructor")
data class User private constructor(
    @SerialName("_id") val id: UserId,
    val name: Name,
    val schoolProfile: SchoolProfile,
    val position: Position,
    val available: Boolean,
) {
    companion object {
        operator fun invoke(
            id: UserId,
            name: Name,
            schoolProfile: SchoolProfile,
            position: Position,
        ) = User(
            id = id,
            name = name,
            schoolProfile = schoolProfile,
            position = position,
            available = true,
        )
    }

    val email: Email by lazy { schoolProfile.email }
    val department: Department by lazy { schoolProfile.department }
    val studentNumber: StudentNumber by lazy { schoolProfile.studentNumber }

    val roles: Set<Role>
        get() = position.roles

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

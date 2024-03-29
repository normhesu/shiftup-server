package app.vercel.shiftup.features.user.account.domain.model

import app.vercel.shiftup.features.user.account.domain.model.value.Name
import app.vercel.shiftup.features.user.domain.model.value.*
import app.vercel.shiftup.features.user.invite.domain.model.value.Position
import kotlinx.serialization.Serializable

@Serializable
data class AvailableUser(
    val id: UserId,
    val name: Name,
    val schoolProfile: SchoolProfile,
    val position: Position,
) {
    val email: Email by lazy { schoolProfile.email }
    val department: Department by lazy { schoolProfile.department }
    val studentNumber: StudentNumber by lazy { schoolProfile.studentNumber }

    val roles: Set<Role>
        get() = position.roles

    fun inSchool(fiscalYear: Int?) = getSchoolYear(fiscalYear) != null

    fun hasRole(role: Role) = role in roles

    private fun getSchoolYear(fiscalYear: Int?) = studentNumber.getSchoolYear(
        tenure = department.tenure,
        fiscalYear = fiscalYear,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AvailableUser
        return id == other.id
    }

    override fun hashCode() = id.hashCode()
}

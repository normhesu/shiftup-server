package app.vercel.shiftup.features.user.account.domain.model

import app.vercel.shiftup.features.user.account.domain.model.value.Name
import app.vercel.shiftup.features.user.account.domain.model.value.Role
import app.vercel.shiftup.features.user.account.domain.model.value.SchoolYear
import app.vercel.shiftup.features.user.domain.model.value.Department
import app.vercel.shiftup.features.user.domain.model.value.NeecEmail
import app.vercel.shiftup.features.user.domain.model.value.StudentNumber
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class UserId(val value: String)

@Serializable
data class User(
    @SerialName("_id") val id: UserId,
    val name: Name,
    val studentNumber: StudentNumber,
    val department: Department,
    val roles: Set<Role>,
) {
    val email: NeecEmail
        get() = NeecEmail.of(studentNumber)
    val schoolYear: SchoolYear?
        get() = SchoolYear.of(
            entranceYear = studentNumber.entranceYear,
            tenure = department.tenure,
        )
}

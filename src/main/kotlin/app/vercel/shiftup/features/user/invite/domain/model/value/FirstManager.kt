package app.vercel.shiftup.features.user.invite.domain.model.value

import app.vercel.shiftup.features.user.domain.model.value.Department
import app.vercel.shiftup.features.user.domain.model.value.Email
import kotlinx.serialization.Serializable

@Serializable
data class FirstManager(
    val email: Email,
    val department: Department,
) {
    val studentNumber = email.studentNumber
}

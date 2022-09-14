package app.vercel.shiftup.features.user.invite.domain.model.value

import app.vercel.shiftup.features.user.domain.model.value.Department
import app.vercel.shiftup.features.user.domain.model.value.StudentNumber
import kotlinx.serialization.Serializable

@Serializable
data class FirstManager(
    val studentNumber: StudentNumber,
    val department: Department,
)

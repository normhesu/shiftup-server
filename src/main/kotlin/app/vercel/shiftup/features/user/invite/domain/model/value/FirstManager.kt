package app.vercel.shiftup.features.user.invite.domain.model.value

import app.vercel.shiftup.features.user.domain.model.value.SchoolProfile
import kotlinx.serialization.Serializable

@Serializable
data class FirstManager(
    val schoolProfile: SchoolProfile,
) {
    val email by lazy { schoolProfile.email }
    val department by lazy { schoolProfile.department }
    val studentNumber by lazy { schoolProfile.studentNumber }
}

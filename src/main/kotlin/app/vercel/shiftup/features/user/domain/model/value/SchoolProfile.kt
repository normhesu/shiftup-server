package app.vercel.shiftup.features.user.domain.model.value

import kotlinx.serialization.Serializable

@Serializable
data class SchoolProfile(
    val email: Email,
    val department: Department,
) {
    val studentNumber by lazy { email.studentNumber }

    init {
        val isNeec = email is NeecEmail && department is NeecDepartment
        val isTeu = email is TeuEmail && department is TeuDepartment
        require(isNeec || isTeu)
    }
}

package app.vercel.shiftup.features.user.account.domain.model

import app.vercel.shiftup.features.user.domain.model.value.Role
import kotlinx.serialization.Serializable

@Serializable
data class CastId constructor(val value: UserId)

@Serializable
@JvmInline
value class Cast(val value: User) {
    val id get() = CastId(value.id)
    fun inSchool(fiscalYear: Int?) = value.inSchool(fiscalYear)

    init {
        require(Role.Cast in value.roles)
    }
}

package app.vercel.shiftup.features.user.account.domain.model

import app.vercel.shiftup.features.user.domain.model.value.Role
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class CastId private constructor(val value: UserId) {
    companion object {
        fun unsafe(value: UserId) = CastId(value)
    }
}

@Serializable
@JvmInline
value class Cast(val value: AvailableUser) {
    val id get() = CastId.unsafe(value.id)
    fun inSchool(fiscalYear: Int?) = value.inSchool(fiscalYear)

    init {
        require(value.hasRole(Role.Cast))
    }
}

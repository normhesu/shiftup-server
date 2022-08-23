package app.vercel.shiftup.features.user.account.domain.model.value

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class SchoolYear(val value: Int) {
    init {
        require(value in MIN..MAX)
    }

    companion object {
        const val MIN = 1
        private const val MAX = 4
    }
}

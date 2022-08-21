package app.vercel.shiftup.features.user.domain.model.value

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class EntranceYear(val value: Int) {
    init {
        require(OPENING_YEAR <= value)
    }
}

private const val OPENING_YEAR = 1987

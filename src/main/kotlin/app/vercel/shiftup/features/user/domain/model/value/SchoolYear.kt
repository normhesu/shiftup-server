package app.vercel.shiftup.features.user.domain.model.value

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class SchoolYear(private val value: Int) {
    init {
        require(value in MIN_VALUE..MAX_VALUE)
    }

    companion object {
        const val MIN_VALUE = 1
        private const val MAX_VALUE = 4
    }

    operator fun plus(year: Int) = SchoolYear(value + year)
}

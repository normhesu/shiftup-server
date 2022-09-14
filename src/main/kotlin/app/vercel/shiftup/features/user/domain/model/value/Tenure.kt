package app.vercel.shiftup.features.user.domain.model.value

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class Tenure(
    @Suppress("unused") private val value: Int,
) : Comparable<Tenure> {
    init {
        require(value in MIN_VALUE..MAX_VALUE)
    }

    companion object {
        private const val MIN_VALUE = 1
        private const val MAX_VALUE = 4
        val MIN = Tenure(MIN_VALUE)
    }

    operator fun rangeTo(other: Tenure) = TenureRange(this, other)
    override fun compareTo(other: Tenure): Int {
        return value.compareTo(other.value)
    }
}

data class TenureRange(
    override val start: Tenure,
    override val endInclusive: Tenure,
) : ClosedRange<Tenure>

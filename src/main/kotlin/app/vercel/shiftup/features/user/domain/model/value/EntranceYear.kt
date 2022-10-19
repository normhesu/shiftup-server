package app.vercel.shiftup.features.user.domain.model.value

import app.vercel.shiftup.features.core.domain.model.fiscalYear
import app.vercel.shiftup.features.core.domain.model.nowTokyoLocalDateTime
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class EntranceYear(private val value: Int) {
    init {
        require(OPENING_YEAR <= value)
    }

    operator fun minus(other: EntranceYear): EntranceYear {
        return EntranceYear(value - other.value)
    }

    /**
     * 入学前や卒業後の場合はnullを返します
     */
    fun getSchoolYear(
        tenure: Tenure,
        fiscalYear: Int? = null,
    ): SchoolYear? {
        val year = fiscalYear ?: Clock.System.nowTokyoLocalDateTime().fiscalYear()
        return (year - value + SchoolYear.MIN_VALUE)
            .takeIf {
                runCatching {
                    Tenure(it)
                }.getOrElse {
                    return null
                } in Tenure.MIN..tenure
            }
            ?.let(::SchoolYear)
    }
}

private const val OPENING_YEAR = 1987

package app.vercel.shiftup.features.user.domain.model.value

import app.vercel.shiftup.features.user.account.domain.model.value.SchoolYear
import kotlinx.serialization.Serializable
import java.time.Month
import java.time.ZoneId
import java.time.ZonedDateTime

@Serializable
@JvmInline
value class EntranceYear(val value: Int) {
    init {
        require(OPENING_YEAR <= value)
    }

    operator fun minus(other: EntranceYear): EntranceYear {
        return EntranceYear(value - other.value)
    }

    /**
     * 入学前や卒業後の場合はnullを返します
     */
    fun getSchoolYear(tenure: Int): SchoolYear? {
        val currentDate = ZonedDateTime.now(ZoneId.of("Asia/Tokyo"))
        val fiscalYear = when (currentDate.month) {
            in Month.JANUARY..Month.MARCH -> currentDate.year - 1
            else -> currentDate.year
        }
        return (fiscalYear - value + SchoolYear.MIN)
            .takeIf { it in SchoolYear.MIN..tenure }
            ?.let(::SchoolYear)
    }
}

private const val OPENING_YEAR = 1987

package app.vercel.shiftup.features.user.account.domain.model.value

import app.vercel.shiftup.features.user.domain.model.value.EntranceYear
import kotlinx.serialization.Serializable
import java.time.Month
import java.time.ZoneId
import java.time.ZonedDateTime

@Serializable
@JvmInline
value class SchoolYear(val value: Int) {
    init {
        require(value in MIN..MAX)
    }

    companion object {
        private const val MIN = 1
        private const val MAX = 4

        /**
         * 入学前や卒業後の場合はnullを返します
         */
        fun of(entranceYear: EntranceYear, tenure: Int): SchoolYear? {
            val currentDate = ZonedDateTime.now(ZoneId.of("Asia/Tokyo"))
            val fiscalYear = when (currentDate.month) {
                in Month.JANUARY..Month.MARCH -> currentDate.year - 1
                else -> currentDate.year
            }
            return (fiscalYear - entranceYear.value + MIN)
                .takeIf { it in MIN..tenure }
                ?.let(::SchoolYear)
        }
    }
}

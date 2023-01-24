package app.vercel.shiftup.features.attendance.domain.model.value

import app.vercel.shiftup.features.core.domain.model.fiscalYear
import app.vercel.shiftup.features.core.domain.model.toTokyoLocalDateTime
import kotlinx.datetime.*
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class OpenCampusDate(
    private val value: LocalDate,
) : Comparable<OpenCampusDate> {
    companion object {
        fun now() = OpenCampusDate(Clock.System.now().toTokyoLocalDateTime().date)

        fun firstDayOfThisMonth(): OpenCampusDate {
            val now = Clock.System.now().toTokyoLocalDateTime().date
            return OpenCampusDate(
                LocalDate(
                    year = now.year,
                    month = now.month,
                    dayOfMonth = 1,
                )
            )
        }

        fun lastDayOfThisMonth(): OpenCampusDate {
            val firstDayOfThisMonth = firstDayOfThisMonth().value
            val lastDayOfThisMonth = firstDayOfThisMonth + DatePeriod(months = 1) - DatePeriod(days = 1)
            return OpenCampusDate(lastDayOfThisMonth)
        }
    }

    val fiscalYear get() = value.fiscalYear()

    fun previousDay() = OpenCampusDate(value - DatePeriod(days = 1))

    operator fun compareTo(other: LocalDate) = value.compareTo(other)
    override operator fun compareTo(other: OpenCampusDate) = value.compareTo(other.value)
}

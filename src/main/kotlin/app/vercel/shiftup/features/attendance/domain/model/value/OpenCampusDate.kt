package app.vercel.shiftup.features.attendance.domain.model.value

import app.vercel.shiftup.features.core.domain.model.fiscalYear
import app.vercel.shiftup.features.core.domain.model.toTokyoLocalDateTime
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class OpenCampusDate(
    private val value: LocalDate,
) : Comparable<OpenCampusDate> {
    companion object {
        fun now() = OpenCampusDate(Clock.System.now().toTokyoLocalDateTime().date)
    }

    val fiscalYear get() = value.fiscalYear()

    operator fun compareTo(other: LocalDate) = value.compareTo(other)
    override operator fun compareTo(other: OpenCampusDate) = value.compareTo(other.value)
}

package app.vercel.shiftup.features.attendance.survey.domain.model.value

import app.vercel.shiftup.features.core.domain.model.fiscalYear
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class OpenCampusDate(
    private val value: LocalDate,
) : Comparable<OpenCampusDate> {
    val fiscalYear get() = value.fiscalYear()

    operator fun compareTo(other: LocalDate) = value.compareTo(other)
    override operator fun compareTo(other: OpenCampusDate) = value.compareTo(other.value)
}

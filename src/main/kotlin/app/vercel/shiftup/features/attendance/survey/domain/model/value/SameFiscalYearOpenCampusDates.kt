package app.vercel.shiftup.features.attendance.survey.domain.model.value

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class SameFiscalYearOpenCampusDates(
    private val value: Set<OpenCampusDate>,
) : Iterable<OpenCampusDate> {
    companion object {
        fun empty() = SameFiscalYearOpenCampusDates(emptySet())
    }

    init {
        require(value.all { it.fiscalYear == fiscalYear }) {
            "全てのオープンキャンパスの日付は、同じ年度である必要があります"
        }
    }

    val fiscalYear get() = value.first().fiscalYear

    fun isNotEmpty() = value.isNotEmpty()

    fun sinceNow() = value.filter { it >= OpenCampusDate.now() }

    fun earliestDateOrThrow() = value.min()
    fun laterDateOrThrow() = value.max()

    fun sorted() = SameFiscalYearOpenCampusDates(value.sorted().toSet())

    operator fun contains(openCampusDate: OpenCampusDate) = value.contains(openCampusDate)

    operator fun plus(other: SameFiscalYearOpenCampusDates) = SameFiscalYearOpenCampusDates(
        (value + other.value).toSet()
    )

    override fun iterator() = value.iterator()
}

fun Collection<SameFiscalYearOpenCampusDates>.flatten() = this.fold(
    SameFiscalYearOpenCampusDates.empty(),
) { acc, openCampusDates ->
    acc + openCampusDates
}.sorted()

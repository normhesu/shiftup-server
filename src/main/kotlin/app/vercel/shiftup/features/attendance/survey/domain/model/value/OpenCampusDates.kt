package app.vercel.shiftup.features.attendance.survey.domain.model.value

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class OpenCampusDates(private val value: Set<OpenCampusDate>) {
    init {
        require(value.all { it.fiscalYear == fiscalYear }) {
            "全てのオープンキャンパスの日付は、同じ年度である必要があります"
        }
    }

    val fiscalYear get() = value.first().fiscalYear

    fun isNotEmpty() = value.isNotEmpty()

    fun earliestDateOrThrow() = value.min()
    fun laterDateOrThrow() = value.max()

    fun <R> map(transform: (OpenCampusDate) -> R): List<R> = value.map(transform)

    fun all(predicate: (OpenCampusDate) -> Boolean): Boolean = value.all(predicate)

    operator fun contains(openCampusDate: OpenCampusDate) = value.contains(openCampusDate)
}

package app.vercel.shiftup.features.schedule.domain.model.value

import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

@Serializable
data class Task(
    private val name: String,
    private val startTime: LocalTime,
    private val endTime: LocalTime,
) {
    init {
        require(name.isNotBlank())
        require(startTime < endTime)
        require(startTime >= LocalTime(hour = 7, minute = 0, second = 0))
        require(endTime <= LocalTime(hour = 19, minute = 0, second = 0))
    }

    fun isNotOverlapBusinessHours(other: Task) = !isOverlapBusinessHours(other)

    private fun isOverlapBusinessHours(other: Task): Boolean {
        return this.startTime < other.endTime && this.endTime > other.startTime
    }
}

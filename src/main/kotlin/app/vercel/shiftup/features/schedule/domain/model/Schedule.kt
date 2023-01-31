package app.vercel.shiftup.features.schedule.domain.model

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.schedule.domain.model.value.CastSchedule
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
@JvmInline
value class ScheduleId(
    @Suppress("unused")
    private val value: OpenCampusDate,
)

@Suppress("DataClassPrivateConstructor")
@Serializable
data class Schedule private constructor(
    val openCampusDate: OpenCampusDate,
    val castSchedules: List<CastSchedule>, // キャストは最大50人程度
    @SerialName("_id") val id: ScheduleId,
) {
    companion object {
        operator fun invoke(
            openCampusDate: OpenCampusDate,
            castSchedules: List<CastSchedule>,
        ) = Schedule(
            openCampusDate = openCampusDate,
            castSchedules = castSchedules,
            id = ScheduleId(openCampusDate)
        )
    }

    init {
        val names = castSchedules.map { it.castName }
        val dropCount = 1
        val nameAndRequireDuplicateCheckNamesList = names zip names
            .drop(dropCount)
            .takeIf { it.isNotEmpty() }
            ?.windowed(
                size = names.size - dropCount,
                step = 1,
                partialWindows = true,
            ).orEmpty()
        val notDuplicateName = nameAndRequireDuplicateCheckNamesList.all { (name, duplicateCheckNames) ->
            duplicateCheckNames.all { (name laxEquals it).not() }
        }
        require(notDuplicateName)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Schedule
        return id == other.id
    }

    override fun hashCode() = id.hashCode()
}

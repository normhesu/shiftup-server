package app.vercel.shiftup.features.schedule.domain.model.value

import app.vercel.shiftup.features.user.account.domain.model.value.Name
import kotlinx.serialization.Serializable

@Serializable
data class CastSchedule(
    val castName: Name,
    private val tasks: Set<Task>, // タスクは最大10個程度
) {

    init {
        val dropCount = 1
        val taskAndRequireOverlapCheckTasksList = tasks zip tasks
            .drop(dropCount)
            .takeIf { it.isNotEmpty() }
            ?.windowed(
                size = tasks.size - dropCount,
                step = 1,
                partialWindows = true,
            ).orEmpty()
        val notOverlapBusinessHours = taskAndRequireOverlapCheckTasksList.all { (task, overlapCheckTasks) ->
            overlapCheckTasks.all { task.isNotOverlapBusinessHours(it) }
        }
        require(notOverlapBusinessHours)
    }
}

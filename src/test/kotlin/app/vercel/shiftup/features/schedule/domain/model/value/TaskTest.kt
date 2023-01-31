package app.vercel.shiftup.features.schedule.domain.model.value

import io.kotest.core.spec.style.FreeSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalTime

class TaskTest : FreeSpec({
    "Task" - {
        "isNotOverlapBusinessHours" {
            data class Param(
                val task1: Task,
                val task2: Task,
                val isOverlap: Boolean,
            )

            fun task(startTime: LocalTime, endTime: LocalTime) = Task(
                name = "taskName", startTime = startTime, endTime = endTime,
            )

            listOf(
                Param(
                    task(LocalTime(7, 0, 0), LocalTime(8, 0, 0)),
                    task(LocalTime(8, 0, 0), LocalTime(9, 0, 0)),
                    isOverlap = false,
                ),
                Param(
                    task(LocalTime(7, 0, 0), LocalTime(8, 0, 0)),
                    task(LocalTime(9, 0, 0), LocalTime(10, 0, 0)),
                    isOverlap = false,
                ),
                Param(
                    task(LocalTime(7, 0, 0), LocalTime(9, 0, 0)),
                    task(LocalTime(8, 0, 0), LocalTime(10, 0, 0)),
                    isOverlap = true,
                ),
                Param(
                    task(LocalTime(7, 0, 0), LocalTime(9, 0, 0)),
                    task(LocalTime(7, 0, 0), LocalTime(10, 0, 0)),
                    isOverlap = true,
                ),
                Param(
                    task(LocalTime(8, 0, 0), LocalTime(8, 0, 1)),
                    task(LocalTime(8, 0, 0), LocalTime(9, 0, 0)),
                    isOverlap = true,
                ),
                Param(
                    task(LocalTime(7, 0, 0), LocalTime(9, 0, 0)),
                    task(LocalTime(8, 59, 59), LocalTime(10, 0, 0)),
                    isOverlap = true,
                ),
                Param(
                    task(LocalTime(7, 0, 0), LocalTime(9, 0, 0)),
                    task(LocalTime(8, 59, 59), LocalTime(9, 0, 0)),
                    isOverlap = true,
                ),
                Param(
                    task(LocalTime(7, 0, 0), LocalTime(10, 0, 0)),
                    task(LocalTime(8, 0, 0), LocalTime(9, 0, 0)),
                    isOverlap = true,
                ),
            ).forAll { (task1, task2, isOverlap) ->
                task1.isNotOverlapBusinessHours(task2) shouldBe !isOverlap
                task2.isNotOverlapBusinessHours(task1) shouldBe !isOverlap
            }
        }
    }
})

package app.vercel.shiftup.features.schedule.application

import app.vercel.shiftup.features.schedule.domain.model.Schedule
import app.vercel.shiftup.features.schedule.infra.ScheduleRepository
import org.koin.core.annotation.Single

@Single
class AddOrReplaceScheduleUseCase(
    private val scheduleRepository: ScheduleRepository,
) {
    suspend operator fun invoke(schedule: Schedule) {
        scheduleRepository.addOrReplace(schedule)
    }
}

package app.vercel.shiftup.features.schedule.application

import app.vercel.shiftup.features.schedule.domain.model.Schedule
import app.vercel.shiftup.features.schedule.domain.model.ScheduleId
import app.vercel.shiftup.features.schedule.infra.ScheduleRepository
import org.koin.core.annotation.Single

@Single
class GetScheduleUseCase(
    private val scheduleRepository: ScheduleRepository,
) {
    suspend operator fun invoke(scheduleId: ScheduleId): Schedule? {
        return scheduleRepository.findById(scheduleId)
    }
}

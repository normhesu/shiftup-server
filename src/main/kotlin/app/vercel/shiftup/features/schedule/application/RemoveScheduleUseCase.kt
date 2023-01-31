package app.vercel.shiftup.features.schedule.application

import app.vercel.shiftup.features.schedule.domain.model.ScheduleId
import app.vercel.shiftup.features.schedule.infra.ScheduleRepository
import com.mongodb.client.result.DeleteResult
import org.koin.core.annotation.Single

@Single
class RemoveScheduleUseCase(
    private val scheduleRepository: ScheduleRepository,
) {
    suspend operator fun invoke(scheduleId: ScheduleId): DeleteResult {
        return scheduleRepository.remove(scheduleId)
    }
}

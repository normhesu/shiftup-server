package app.vercel.shiftup.features.schedule.application

import app.vercel.shiftup.features.schedule.infra.GetAllScheduleIdQueryService
import org.koin.core.annotation.Single

@Single
class GetAllScheduleIdUseCase(
    private val getAllScheduleIdQueryService: GetAllScheduleIdQueryService,
) {
    suspend operator fun invoke() = getAllScheduleIdQueryService().toSet()
}

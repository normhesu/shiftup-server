package app.vercel.shiftup.presentation.routes.schedule

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.schedule.application.AddOrReplaceScheduleUseCase
import app.vercel.shiftup.features.schedule.application.GetAllScheduleIdUseCase
import app.vercel.shiftup.features.schedule.application.GetScheduleUseCase
import app.vercel.shiftup.features.schedule.application.RemoveScheduleUseCase
import app.vercel.shiftup.features.schedule.domain.model.Schedule
import app.vercel.shiftup.features.schedule.domain.model.ScheduleId
import app.vercel.shiftup.features.schedule.domain.model.value.CastSchedule
import app.vercel.shiftup.features.user.domain.model.value.Role
import app.vercel.shiftup.features.user.invite.application.*
import app.vercel.shiftup.presentation.plugins.routingWithRole
import app.vercel.shiftup.presentation.routes.inject
import app.vercel.shiftup.presentation.routes.respondDeleteResult
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.put
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable
import org.mpierce.ktor.csrf.noCsrfProtection

fun Application.schedulesRouting() {
    castRouting()
    managerRouting()
}

private fun Application.castRouting() = routingWithRole(Role.Cast) {
    noCsrfProtection {
        get<Schedules> {
            val useCase: GetAllScheduleIdUseCase by inject()
            call.respond(useCase())
        }
        get<Schedules.Id> {
            @Serializable
            data class Response(
                val id: ScheduleId,
                val openCampusDate: OpenCampusDate,
                val castSchedules: List<CastSchedule>,
            )

            val useCase: GetScheduleUseCase by inject()
            val schedule = useCase(it.id)
            if (schedule != null) {
                val response = Response(
                    id = schedule.id,
                    openCampusDate = schedule.openCampusDate,
                    castSchedules = schedule.castSchedules,
                )
                call.respond(response)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}

private fun Application.managerRouting() = routingWithRole(Role.Manager) {
    put<Schedules> {
        @Serializable
        data class Params(
            val openCampusDate: OpenCampusDate,
            val castSchedules: List<CastSchedule>,
        )

        val params: Params = call.receive()
        val schedule = Schedule(
            openCampusDate = params.openCampusDate,
            castSchedules = params.castSchedules,
        )
        val useCase: AddOrReplaceScheduleUseCase by inject()
        useCase(schedule)

        call.respond(HttpStatusCode.NoContent)
    }
    delete<Schedules.Id> {
        val useCase: RemoveScheduleUseCase by inject()
        val result = useCase(it.id)
        call.respondDeleteResult(result)
    }
}

@Suppress("unused")
@Serializable
@Resource("schedules")
class Schedules {
    @Serializable
    @Resource("{id}")
    class Id(val parent: Schedules, val id: ScheduleId)
}

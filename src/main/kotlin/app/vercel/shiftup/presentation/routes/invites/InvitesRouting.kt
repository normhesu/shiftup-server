package app.vercel.shiftup.presentation.routes.invites

import app.vercel.shiftup.features.user.account.application.GetAvailableUsersByStudentNumberUseCase
import app.vercel.shiftup.features.user.account.domain.model.value.Name
import app.vercel.shiftup.features.user.domain.model.value.Department
import app.vercel.shiftup.features.user.domain.model.value.Role
import app.vercel.shiftup.features.user.domain.model.value.StudentNumber
import app.vercel.shiftup.features.user.invite.application.AddInviteUseCase
import app.vercel.shiftup.features.user.invite.application.GetAllInvitesUseCase
import app.vercel.shiftup.features.user.invite.application.RemoveInviteUseCase
import app.vercel.shiftup.features.user.invite.domain.model.InviteId
import app.vercel.shiftup.features.user.invite.domain.model.value.Position
import app.vercel.shiftup.presentation.routes.auth.plugins.routingWithRole
import app.vercel.shiftup.presentation.routes.inject
import app.vercel.shiftup.presentation.routes.respondDeleteResult
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.mpierce.ktor.csrf.noCsrfProtection

fun Application.invitesRouting() = routingWithRole(Role.Manager) {
    noCsrfProtection {
        get<Invites> {
            @Serializable
            data class ResponseItem(
                val id: InviteId,
                val studentNumber: StudentNumber,
                val name: Name?,
                val department: Department,
                val position: Position,
            )

            val getAllInvitesUseCase: GetAllInvitesUseCase by inject()
            val getAvailableUsersByStudentNumberUseCase: GetAvailableUsersByStudentNumberUseCase by inject()

            val invites = getAllInvitesUseCase()
            val names: Map<StudentNumber, Name> = getAvailableUsersByStudentNumberUseCase(
                invites.map { it.studentNumber },
            ).associate {
                it.studentNumber to it.name
            }

            val response = getAllInvitesUseCase().map {
                ResponseItem(
                    id = it.id,
                    studentNumber = it.studentNumber,
                    name = names[it.studentNumber],
                    department = it.department,
                    position = it.position
                )
            }
            call.respond(response)
        }
    }
    post<Invites> {
        val useCase: AddInviteUseCase by inject()
        useCase(invite = call.receive())
            .onSuccess {
                call.respond(HttpStatusCode.Created)
            }.onFailure {
                call.response.headers.append(
                    name = HttpHeaders.Allow,
                    value = listOf(HttpMethod.Get, HttpMethod.Delete)
                        .joinToString { it.value },
                )
                call.respond(HttpStatusCode.MethodNotAllowed)
            }
    }
    delete<Invites.Id> {
        val useCase: RemoveInviteUseCase by inject()
        call.respondDeleteResult(
            useCase(inviteId = InviteId(StudentNumber(it.id)))
        )
    }
}

@Suppress("unused")
@Serializable
@Resource("invites")
class Invites {
    @Serializable
    @Resource("{id}")
    class Id(val parent: Invites, val id: String)
}
package app.vercel.shiftup.presentation.routes.invites

import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.account.domain.model.value.Name
import app.vercel.shiftup.features.user.domain.model.value.Department
import app.vercel.shiftup.features.user.domain.model.value.Email
import app.vercel.shiftup.features.user.domain.model.value.Role
import app.vercel.shiftup.features.user.domain.model.value.StudentNumber
import app.vercel.shiftup.features.user.invite.application.*
import app.vercel.shiftup.features.user.invite.domain.model.Invite
import app.vercel.shiftup.features.user.invite.domain.model.InviteId
import app.vercel.shiftup.features.user.invite.domain.model.value.FirstManager
import app.vercel.shiftup.features.user.invite.domain.model.value.Position
import app.vercel.shiftup.presentation.routes.auth.plugins.routingWithRole
import app.vercel.shiftup.presentation.routes.auth.plugins.userId
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
import io.ktor.server.resources.put
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable
import org.mpierce.ktor.csrf.noCsrfProtection

fun Application.invitesRouting() = routingWithRole(Role.Manager) {
    noCsrfProtection {
        get<Invites> {
            @Serializable
            data class ResponseItem(
                val id: InviteId,
                val userId: UserId?,
                val name: Name?,
                val studentNumber: StudentNumber,
                val email: Email?,
                val department: Department,
                val position: Position,
            )

            val useCase: GetAllInvitesWithAvailableUserOrNullUseCase by inject()
            val response = useCase().map { (invite, availableUserOrNull) ->
                ResponseItem(
                    id = invite.id,
                    userId = availableUserOrNull?.id,
                    name = availableUserOrNull?.name,
                    studentNumber = invite.studentNumber,
                    email = availableUserOrNull?.email,
                    department = invite.department,
                    position = invite.position
                )
            }

            call.respond(response)
        }

        get<Invites.First.Id> {
            val firstManager: FirstManager by inject()
            call.respond(Invite(firstManager).id)
        }
    }

    postInvite()

    put<Invites.Id.Position> {
        val useCase: ChangeInvitePositionUseCase by inject()
        useCase(
            inviteId = it.parent.id,
            position = enumValueOf(call.receiveText()),
            operatorId = call.sessions.userId,
        ).onSuccess {
            call.respond(HttpStatusCode.NoContent)
        }.onFailure { e ->
            when (e) {
                is ChangeInvitePositionUseCaseException.UnsupportedOperation -> {
                    call.response.headers.append(HttpHeaders.Allow, "")
                    call.respond(HttpStatusCode.MethodNotAllowed)
                }
            }
        }
    }

    delete<Invites.Id> {
        val useCase: RemoveInviteUseCase by inject()
        call.respondDeleteResult(
            useCase(inviteId = it.id)
        )
    }
}

private fun Route.postInvite() = post<Invites> {
    @Serializable
    data class Params(
        val studentNumber: StudentNumber,
        val department: Department,
        val position: Position,
    )

    val params: Params = call.receive()
    val invite = Invite(
        studentNumber = params.studentNumber,
        department = params.department,
        position = params.position,
    )

    val useCase: AddInviteUseCase by inject()
    useCase(invite).onSuccess {
        call.respond(HttpStatusCode.Created)
    }.onFailure { e ->
        when (e) {
            is AddInviteUseCaseException.Invited -> {
                call.response.headers.append(
                    name = HttpHeaders.Allow,
                    value = listOf(HttpMethod.Get, HttpMethod.Delete)
                        .joinToString { it.value },
                )
                call.respond(HttpStatusCode.MethodNotAllowed)
            }
        }
    }
}

@Suppress("unused")
@Serializable
@Resource("invites")
class Invites {
    @Serializable
    @Resource("first")
    class First(val parent: Invites) {
        @Serializable
        @Resource("id")
        class Id(val parent: First)
    }

    @Serializable
    @Resource("{id}")
    class Id(val parent: Invites, val id: InviteId) {
        @Serializable
        @Resource("position")
        class Position(val parent: Id)
    }
}

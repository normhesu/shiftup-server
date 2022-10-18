package app.vercel.shiftup.presentation.routes.invites

import app.vercel.shiftup.features.user.domain.model.value.Role
import app.vercel.shiftup.features.user.invite.application.AddInviteUseCase
import app.vercel.shiftup.features.user.invite.application.GetAllInvitesUseCase
import app.vercel.shiftup.features.user.invite.application.RemoveInviteUseCase
import app.vercel.shiftup.features.user.invite.application.ReplaceInviteUseCase
import app.vercel.shiftup.features.user.invite.domain.model.InviteId
import app.vercel.shiftup.presentation.routes.auth.plugins.withRole
import app.vercel.shiftup.presentation.routes.respondDeleteResult
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.patch
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

fun Application.invitesRouting() {
    routing {
        authenticate {
            withRole(Role.Manager) {
                get<Invites> {
                    val useCase: GetAllInvitesUseCase
                        by this@invitesRouting.inject()

                    call.respond(useCase())
                }
                post<Invites> {
                    val useCase: AddInviteUseCase
                        by this@invitesRouting.inject()

                    useCase(call.receive())
                    call.respond(HttpStatusCode.Created)
                }
                patch<Invites> {
                    val useCase: ReplaceInviteUseCase
                        by this@invitesRouting.inject()

                    useCase(call.receive())
                    call.respond(HttpStatusCode.NoContent)
                }
                delete<Invites.Id> {
                    val useCase: RemoveInviteUseCase
                        by this@invitesRouting.inject()

                    call.respondDeleteResult(
                        useCase(inviteId = InviteId(it.id))
                    )
                }
            }
        }
    }
}

@Serializable
@Resource("/invites")
object Invites {
    @Serializable
    @Resource("{id}")
    class Id(val parent: Invites = Invites, val id: String)
}

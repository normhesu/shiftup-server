package app.vercel.shiftup.presentation.routes.invitedusers

import app.vercel.shiftup.features.user.domain.model.value.Role
import app.vercel.shiftup.features.user.invited.application.AddInvitedUsersUseCase
import app.vercel.shiftup.features.user.invited.application.GetAllInvitedUsersUseCase
import app.vercel.shiftup.features.user.invited.application.RemoveInvitedUsersUseCase
import app.vercel.shiftup.features.user.invited.application.ReplaceInvitedUsersUseCase
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

fun Application.invitedUsersRouting() {
    routing {
        authenticate {
            withRole(Role.Manager) {
                get<InvitedUsers> {
                    val useCase: GetAllInvitedUsersUseCase
                        by this@invitedUsersRouting.inject()

                    call.respond(useCase())
                }
                post<InvitedUsers> {
                    val useCase: AddInvitedUsersUseCase
                        by this@invitedUsersRouting.inject()

                    useCase(call.receive())
                    call.respond(HttpStatusCode.Created)
                }
                patch<InvitedUsers> {
                    val useCase: ReplaceInvitedUsersUseCase
                        by this@invitedUsersRouting.inject()

                    useCase(call.receive())
                    call.respond(HttpStatusCode.NoContent)
                }
                delete<InvitedUsers> {
                    val useCase: RemoveInvitedUsersUseCase
                        by this@invitedUsersRouting.inject()

                    call.respondDeleteResult(
                        useCase(call.receive())
                    )
                }
            }
        }
    }
}

@Serializable
@Resource("/invited-users")
object InvitedUsers

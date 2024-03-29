package app.vercel.shiftup.presentation.routes.users

import app.vercel.shiftup.features.user.account.application.*
import app.vercel.shiftup.features.user.account.domain.model.value.Name
import app.vercel.shiftup.features.user.domain.model.value.Department
import app.vercel.shiftup.features.user.invite.domain.model.value.Position
import app.vercel.shiftup.presentation.plugins.userId
import app.vercel.shiftup.presentation.routes.inject
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.put
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable
import org.mpierce.ktor.csrf.noCsrfProtection

fun Application.usersRouting() = routing {
    authenticate {
        noCsrfProtection {
            get<Users.Me> {
                @Serializable
                data class Response(
                    val name: Name,
                    val department: Department,
                    val position: Position,
                )

                val useCase: GetAvailableUserUseCase by inject()
                val availableUser = useCase(call.sessions.userId).let(::checkNotNull)
                val response = Response(
                    name = availableUser.name,
                    department = availableUser.department,
                    position = availableUser.position,
                )
                call.respond(response)
            }

            get<Users.Me.Id> {
                call.respond(call.sessions.userId)
            }

            get<Users.Me.Detail> {
                val useCase: GetAvailableUserDetailUseCase by inject()
                call.respond(
                    useCase(call.sessions.userId)
                )
            }

            get<Users.Me.Roles> {
                val useCase: GetUserRolesUseCase by inject()
                val roles = useCase(call.sessions.userId).let(::checkNotNull)
                call.respond(roles)
            }

            get<Users.Me.Name> {
                val useCase: GetUserNameUseCase by inject()
                val name = useCase(call.sessions.userId).let(::checkNotNull)
                call.respond(name)
            }
        }
        put<Users.Me.Name> {
            val useCase: ChangeUserNameUseCase by inject()
            useCase(
                userId = call.sessions.userId,
                name = Name(call.receiveText()),
            )
            call.respond(HttpStatusCode.NoContent)
        }
        put<Users.Me.Department> {
            val useCase: ChangeAvailableUserDepartmentUseCase by inject()
            useCase(
                userId = call.sessions.userId,
                department = Department.valueOf(call.receiveText())
            )
            call.respond(HttpStatusCode.NoContent)
        }
    }
}

@Suppress("unused")
@Serializable
@Resource("users")
class Users {
    @Serializable
    @Resource("me")
    class Me(val parent: Users) {
        @Serializable
        @Resource("id")
        class Id(val parent: Me)

        @Serializable
        @Resource("detail")
        class Detail(val parent: Me)

        @Serializable
        @Resource("roles")
        class Roles(val parent: Me)

        @Serializable
        @Resource("name")
        class Name(val parent: Me)

        @Serializable
        @Resource("department")
        class Department(val parent: Me)

        @Serializable
        @Resource("attendance")
        class Attendance(val parent: Me)
    }
}

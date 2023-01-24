package app.vercel.shiftup.presentation.routes.users

import app.vercel.shiftup.features.user.account.application.*
import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.account.domain.model.value.Name
import app.vercel.shiftup.features.user.domain.model.value.Department
import app.vercel.shiftup.features.user.domain.model.value.Role
import app.vercel.shiftup.presentation.routes.auth.plugins.routingWithRole
import app.vercel.shiftup.presentation.routes.auth.plugins.userId
import app.vercel.shiftup.presentation.routes.inject
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
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

fun Application.usersRouting() {
    authRouting()
    managerRouting()
}

private fun Application.authRouting() = routing {
    authenticate {
        noCsrfProtection {
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
            val useCase: ChangeUserDepartmentUseCase by inject()
            useCase(
                userId = call.sessions.userId,
                department = Department.valueOf(call.receiveText())
            )
            call.respond(HttpStatusCode.NoContent)
        }
    }
}

private fun Application.managerRouting() = routingWithRole(Role.Manager) {
    put<Users.Id.Position> {
        val useCase: ChangeUserPositionUseCase by inject()
        useCase(
            userId = it.parent.id,
            position = enumValueOf(call.receiveText()),
            operatorId = call.sessions.userId,
        ).onSuccess {
            call.respond(HttpStatusCode.NoContent)
        }.onFailure { e ->
            when (e) {
                is ChangeUserPositionUseCaseException.UserNotFound -> {
                    throw NotFoundException()
                }

                is ChangeUserPositionUseCaseException.UnsupportedOperation -> {
                    call.response.headers.append(HttpHeaders.Allow, "")
                    call.respond(HttpStatusCode.MethodNotAllowed)
                }
            }
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
        class Department(val parent: Id)

        @Serializable
        @Resource("attendance")
        class Attendance(val parent: Me)
    }

    @Serializable
    @Resource("{id}")
    class Id(val parent: Users, val id: UserId) {
        @Serializable
        @Resource("position")
        class Position(val parent: Id)
    }
}

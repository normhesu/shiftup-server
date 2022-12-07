package app.vercel.shiftup.presentation.routes.users

import app.vercel.shiftup.features.user.account.application.GetUserRolesUseCase
import app.vercel.shiftup.presentation.routes.auth.plugins.userId
import app.vercel.shiftup.presentation.routes.inject
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable
import org.mpierce.ktor.csrf.noCsrfProtection

fun Application.usersRouting() = routing {
    authenticate {
        noCsrfProtection {
            get<Users.Me.Roles> {
                val getUserRolesUseCase: GetUserRolesUseCase by inject()
                val roles = call.sessions.userId?.let {
                    getUserRolesUseCase(it)
                }.let(::checkNotNull)
                call.respond(roles)
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
        @Resource("roles")
        class Roles(val parent: Me)
    }
}

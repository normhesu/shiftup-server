package app.vercel.shiftup.presentation.routes.users

import app.vercel.shiftup.features.user.account.application.GetUserUseCase
import app.vercel.shiftup.presentation.routes.auth.plugins.userId
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
import org.koin.ktor.ext.inject
import org.mpierce.ktor.csrf.noCsrfProtection

fun Application.usersRouting() = routing {
    authenticate {
        noCsrfProtection {
            get<Users.Me.Roles> {
                val useCase: GetUserUseCase by application.inject()
                val roles = call.sessions.userId?.let {
                    useCase(it)?.roles
                } ?: throw NotFoundException()
                call.respond(roles)
            }
        }
    }
}

@Suppress("unused")
@Serializable
@Resource("/users")
object Users {
    @Serializable
    @Resource("me")
    class Me(val parent: Users = Users) {
        @Serializable
        @Resource("roles")
        class Roles(val parent: Me = Me())
    }
}

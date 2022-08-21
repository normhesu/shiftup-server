package app.vercel.shiftup.presentation.routes.auth.plugins

import app.vercel.shiftup.features.user.account.application.GetUserUseCase
import app.vercel.shiftup.features.user.account.domain.model.value.Role
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.koin.ktor.ext.inject

fun Route.withRole(role: Role, route: Route.() -> Unit) {
    install(AuthorizationPlugin) {
        allowRole = role
    }
    route()
}

private val AuthorizationPlugin = createRouteScopedPlugin(
    name = "AuthorizationPlugin",
    createConfiguration = ::PluginConfiguration
) {
    val getUserUseCase: GetUserUseCase by application.inject()
    on(AuthenticationChecked) { call ->
        val userRoles = call.sessions.userId?.let {
            getUserUseCase(it)?.roles
        }.orEmpty()

        if (pluginConfig.allowRole !in userRoles) {
            call.respond(HttpStatusCode.Forbidden)
        }
    }
}

private class PluginConfiguration {
    var allowRole: Role? = null
}

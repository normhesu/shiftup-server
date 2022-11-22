package app.vercel.shiftup.presentation.routes.auth.plugins

import app.vercel.shiftup.features.user.account.application.GetUserUseCase
import app.vercel.shiftup.features.user.domain.model.value.Role
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.koin.ktor.ext.inject

// プラグインを誤って複数回インストールしないようにroutingで囲む
fun Application.routingWithRole(role: Role, route: Route.() -> Unit) = routing {
    authenticate {
        install(AuthorizationPlugin) {
            allowRole = role
        }
        route()
    }
}

private val AuthorizationPlugin = createRouteScopedPlugin(
    name = "AuthorizationPlugin",
    createConfiguration = ::PluginConfiguration
) {
    val getUserUseCase: GetUserUseCase by application.inject()
    on(AuthenticationChecked) { call ->
        val user = call.sessions.userId?.let {
            getUserUseCase(it)
        }
        val allow = pluginConfig.allowRole?.let {
            user?.hasRole(it)
        } ?: false

        if (!allow) {
            call.respond(HttpStatusCode.Forbidden)
        }
    }
}

private class PluginConfiguration {
    var allowRole: Role? = null
}

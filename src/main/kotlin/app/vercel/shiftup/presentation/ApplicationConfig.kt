package app.vercel.shiftup.presentation

import io.ktor.server.config.*

val ApplicationConfig.allowAllHosts
    get() = this
        .propertyOrNull(ApplicationConfPath.Ktor.Security.allowAllHosts)
        ?.getString().toBoolean()

private object ApplicationConfPath {
    object Ktor {
        private const val PATH = "ktor"

        object Security {
            private const val PATH = "${Ktor.PATH}.security"
            const val allowAllHosts = "$PATH.allow-all-hosts"
        }
    }
}

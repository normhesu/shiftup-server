package app.vercel.shiftup.presentation

import io.ktor.server.config.*

val ApplicationConfig.allowAllHosts
    get() = this
        .propertyOrNull(ApplicationConfPath.Ktor.Security.allowAllHosts)
        ?.getString().toBoolean()

val ApplicationConfig.mongoDbConnectionUri
    get() = this
        .propertyOrNull(ApplicationConfPath.Ktor.Database.mongoDbConnectionUri)
        ?.getString()

private object ApplicationConfPath {
    object Ktor {
        private const val PATH = "ktor"

        object Security {
            private const val PATH = "${Ktor.PATH}.security"
            const val allowAllHosts = "$PATH.allow-all-hosts"
        }

        object Database {
            private const val PATH = "${Ktor.PATH}.database"
            const val mongoDbConnectionUri = "$PATH.mongo-db-connection-uri"
        }
    }
}

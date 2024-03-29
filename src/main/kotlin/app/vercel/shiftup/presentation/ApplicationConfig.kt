package app.vercel.shiftup.presentation

import app.vercel.shiftup.features.user.domain.model.value.Department
import app.vercel.shiftup.features.user.domain.model.value.Email
import app.vercel.shiftup.features.user.domain.model.value.SchoolProfile
import app.vercel.shiftup.features.user.invite.domain.model.value.FirstManager
import io.ktor.server.config.*

// Application
val ApplicationConfig.serverRootUrl
    get() = this
        .property(ApplicationConfPath.Ktor.Application.serverRootUrl)
        .getString()

val ApplicationConfig.topPageUrl
    get() = this
        .property(ApplicationConfPath.Ktor.Application.topPageUrl)
        .getString()

// Auth
val ApplicationConfig.googleClientId
    get() = this
        .property(ApplicationConfPath.Ktor.Auth.googleClientId)
        .getString()

val ApplicationConfig.googleClientSecret
    get() = this
        .property(ApplicationConfPath.Ktor.Auth.googleClientSecret)
        .getString()

val ApplicationConfig.sessionSignKey
    get() = this
        .property(ApplicationConfPath.Ktor.Auth.sessionSignKey)
        .getString()

val ApplicationConfig.firstManager
    get() = FirstManager(
        SchoolProfile(
            email = firstManagerEmail,
            department = firstManagerDepartment,
        )
    )

private val ApplicationConfig.firstManagerEmail
    get() = this
        .property(ApplicationConfPath.Ktor.Auth.firstManagerEmail)
        .getString()
        .let { Email(it) }

private val ApplicationConfig.firstManagerDepartment
    get() = this
        .property(ApplicationConfPath.Ktor.Auth.firstManagerDepartment)
        .getString()
        .let { Department.valueOf(it) }

// Security
val ApplicationConfig.allowAllHosts
    get() = this
        .propertyOrNull(ApplicationConfPath.Ktor.Security.allowAllHosts)
        ?.getString().toBoolean()

// Database
val ApplicationConfig.mongoDbConnectionUri
    get() = this
        .propertyOrNull(ApplicationConfPath.Ktor.Database.mongoDbConnectionUri)
        ?.getString()

private object ApplicationConfPath {
    object Ktor {
        private const val PATH = "ktor"

        object Application {
            private const val PATH = "${Ktor.PATH}.application"
            const val serverRootUrl = "$PATH.server-root-url"
            const val topPageUrl = "$PATH.top-page-url"
        }

        object Auth {
            private const val PATH = "${Ktor.PATH}.auth"
            const val googleClientId = "$PATH.google-client-id"
            const val googleClientSecret = "$PATH.google-client-secret"
            const val sessionSignKey = "$PATH.session-sign-key"
            const val firstManagerEmail = "$PATH.first-manager-email"
            const val firstManagerDepartment = "$PATH.first-manager-department"
        }

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

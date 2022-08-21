package app.vercel.shiftup.presentation

import io.ktor.server.config.*

// Application
val ApplicationConfig.topPageUrl
    get() = this
        .property(ApplicationConfPath.Ktor.Application.topPageUrl)
        .getString()

// Auth
val ApplicationConfig.firstManager
    get() = FirstManager(
        studentNumber = firstManagerStudentNumber,
        department = firstManagerDepartment,
    )

private val ApplicationConfig.firstManagerStudentNumber
    get() = this
        .property(ApplicationConfPath.Ktor.Auth.firstManagerStudentNumber)
        .getString()
        .let(::StudentNumber)

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
            const val topPageUrl = "$PATH.top-page-url"
        }

        object Auth {
            private const val PATH = "${Ktor.PATH}.auth"
            const val firstManagerStudentNumber = "$PATH.first-manager-student-number"
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

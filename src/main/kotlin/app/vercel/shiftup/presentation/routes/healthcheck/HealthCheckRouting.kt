package app.vercel.shiftup.presentation.routes.healthcheck

import app.vercel.shiftup.features.user.account.application.GetAvailableUsersByIdUseCase
import app.vercel.shiftup.features.user.account.domain.model.UserId
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.resources.*
import io.ktor.server.resources.patch
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import org.mpierce.ktor.csrf.noCsrfProtection

fun Application.healthCheckRouting() {
    routing {
        noCsrfProtection {
            get<HealthCheck> {
                call.respondText("OK")
            }
            get<HealthCheck.DB> {
                val getAvailableUsersByIdUseCase: GetAvailableUsersByIdUseCase by application.inject()
                getAvailableUsersByIdUseCase(listOf(UserId("")))
                call.respondText("DB OK")
            }
            authenticate {
                get<HealthCheck.Authentication> {
                    call.respondText("Authentication OK")
                }
            }
        }
        post<HealthCheck.CSRF> {
            call.respondText("CSRF OK")
        }
        patch<HealthCheck.CSRF> {
            call.respondText("CSRF OK")
        }
        put<HealthCheck.CSRF> {
            call.respondText("CSRF OK")
        }
        delete<HealthCheck.CSRF> {
            call.respondText("CSRF OK")
        }
    }
}

@Suppress("unused")
@Serializable
@Resource("health-check")
class HealthCheck {
    @Serializable
    @Resource("db")
    class DB(val parent: HealthCheck)

    @Serializable
    @Resource("csrf")
    class CSRF(val parent: HealthCheck)

    @Serializable
    @Resource("authentication")
    class Authentication(val parent: HealthCheck)
}

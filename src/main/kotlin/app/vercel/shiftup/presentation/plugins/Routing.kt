package app.vercel.shiftup.presentation.plugins

import app.vercel.shiftup.presentation.routes.routes
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            when (cause) {
                is NotFoundException -> call.respond(HttpStatusCode.NotFound)
                is IllegalArgumentException -> call.respondText(
                    status = HttpStatusCode.UnprocessableEntity,
                    cause = cause,
                )
                else -> call.respondText(
                    status = HttpStatusCode.InternalServerError,
                    cause = cause.takeIf { call.application.developmentMode }
                )
            }
            throw cause
        }
        if (this@configureRouting.developmentMode) {
            val respondTextStatusCodes = HttpStatusCode.allStatusCodes.toTypedArray()
            @Suppress("SpreadOperator")
            status(*respondTextStatusCodes) { call, status ->
                call.respondText(status)
            }
        }
    }
    install(AutoHeadResponse)
    install(Resources)
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                isLenient = true
            }
        )
    }
    routes()
}

private suspend fun ApplicationCall.respondText(
    status: HttpStatusCode,
    cause: Throwable? = null,
    contentType: ContentType? = null,
    configure: OutgoingContent.() -> Unit = {}
) {
    val statusText = "${status.value} ${status.description}"
    val causeText = cause?.let { "\n\n$it" }.orEmpty()
    this.respondText(
        text = statusText + causeText,
        status = status,
        contentType = contentType,
        configure = configure,
    )
}

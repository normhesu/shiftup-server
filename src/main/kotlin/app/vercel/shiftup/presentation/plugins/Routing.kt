package app.vercel.shiftup.presentation.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.resources.*

fun Application.configureRouting() {
    install(StatusPages) {
    }
    install(AutoHeadResponse)
    install(Resources)
    install(ContentNegotiation) {
        json()
    }
}

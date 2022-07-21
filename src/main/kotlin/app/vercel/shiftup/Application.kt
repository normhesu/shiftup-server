package app.vercel.shiftup

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import app.vercel.shiftup.plugins.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureSerialization()
        configureMonitoring()
        configureHTTP()
        configureSecurity()
        configureRouting()
    }.start(wait = true)
}

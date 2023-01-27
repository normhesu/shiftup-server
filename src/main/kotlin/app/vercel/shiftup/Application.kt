package app.vercel.shiftup

import app.vercel.shiftup.presentation.plugins.*
import io.ktor.server.application.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    configureDI()
    configureMonitoring()
    configureSecurity()
    configureRouting()
}

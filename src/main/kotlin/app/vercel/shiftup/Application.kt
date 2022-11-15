package app.vercel.shiftup

import app.vercel.shiftup.presentation.plugins.configureDI
import app.vercel.shiftup.presentation.plugins.configureMonitoring
import app.vercel.shiftup.presentation.plugins.configureRouting
import app.vercel.shiftup.presentation.plugins.configureSecurity
import io.ktor.server.application.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    configureDI()
    configureMonitoring()
    configureSecurity()
    configureRouting()
}

package app.vercel.shiftup

import app.vercel.shiftup.presentation.plugins.configureMonitoring
import app.vercel.shiftup.presentation.plugins.configureRouting
import app.vercel.shiftup.presentation.plugins.configureSecurity
import io.ktor.server.application.*

@Suppress("RemoveRedundantQualifierName")
fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    configureMonitoring()
    configureSecurity()
    configureRouting()
}

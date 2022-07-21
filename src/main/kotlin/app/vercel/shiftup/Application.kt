package app.vercel.shiftup

import app.vercel.shiftup.plugins.*
import io.ktor.server.application.*

@Suppress("RemoveRedundantQualifierName")
fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureSecurity()
    configureRouting()
}

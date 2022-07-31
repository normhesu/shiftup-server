package app.vercel.shiftup.presentation.plugins

import app.vercel.shiftup.kmongoModule
import io.ktor.server.application.*
import org.koin.core.annotation.Single
import org.koin.ksp.generated.defaultModule
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureDI() {
    install(Koin) {
        slf4jLogger()
        modules(defaultModule, kmongoModule)
    }
}

@Single
@Deprecated("KoinのdefaultModule生成のみに使用します")
class DefaultModuleGeneration

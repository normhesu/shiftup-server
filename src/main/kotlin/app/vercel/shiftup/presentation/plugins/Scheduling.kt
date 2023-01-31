package app.vercel.shiftup.presentation.plugins

import io.ktor.server.application.*
import io.ktor.util.logging.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration

fun Application.schedule(
    name: String,
    fixedDelay: Duration,
    runStartup: Boolean = false,
    task: suspend () -> Unit,
) = launch {
    if (!runStartup) delay(fixedDelay)
    while (true) {
        invokeTask(name, task)
        delay(fixedDelay)
    }
}

private suspend fun Application.invokeTask(
    name: String,
    task: suspend () -> Unit,
) = runCatching {
    log.info("Run $name")
    task()
}.onSuccess {
    log.info("Success $name")
}.onFailure {
    when (it) {
        is CancellationException -> {
            log.info("Cancel $name")
            throw it
        }

        else -> {
            // CancellationException以外が投げられても終了しない
            log.error("Failed $name")
            log.error(it)
        }
    }
}

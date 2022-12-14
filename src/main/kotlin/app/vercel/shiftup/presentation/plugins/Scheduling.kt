package app.vercel.shiftup.presentation.plugins

import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import io.ktor.server.application.*
import io.ktor.util.logging.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration

fun Application.configureScheduling() {
}

private fun Application.schedule(
    name: String,
    fixedDelay: Duration,
    runStartup: Boolean = false,
    task: suspend () -> Unit,
) = launch {
    // CancellationException以外が投げられても終了しない
    suspend fun runTask() = runSuspendCatching {
        log.info("Run $name")
        task()
    }.onSuccess {
        log.info("Success $name")
    }.onFailure {
        log.error("Failed $name")
        log.error(it)
    }

    if (runStartup) runTask()
    while (true) {
        delay(fixedDelay)
        runTask()
    }
}

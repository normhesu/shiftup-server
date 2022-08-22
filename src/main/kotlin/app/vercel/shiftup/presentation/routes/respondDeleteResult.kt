package app.vercel.shiftup.presentation.routes

import com.mongodb.client.result.DeleteResult
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

suspend fun ApplicationCall.respondDeleteResult(
    deleteResult: DeleteResult,
) = when (deleteResult.deletedCount) {
    0L -> respond(HttpStatusCode.NotFound)
    else -> respond(HttpStatusCode.NoContent)
}

package app.vercel.shiftup.features.core.infra

import org.koin.core.annotation.Single
import org.litote.kmongo.coroutine.CoroutineClient

@Single
class Transaction(
    private val mongoClient: CoroutineClient,
) {
    suspend operator fun <R> invoke(block: suspend () -> R) = mongoClient.startSession().use {
        it.startTransaction()
        val result = block()
        it.commitTransaction()
        result
    }
}

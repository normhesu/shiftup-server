package app.vercel.shiftup.features.core.infra

import com.mongodb.bulk.BulkWriteResult
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.InsertManyResult
import com.mongodb.client.result.InsertOneResult
import com.mongodb.client.result.UpdateResult
import io.ktor.utils.io.errors.*

fun InsertOneResult.orThrow(): InsertOneResult {
    if (!wasAcknowledged()) throw IOException()
    return this
}

fun InsertManyResult.orThrow(): InsertManyResult {
    if (!wasAcknowledged()) throw IOException()
    return this
}

fun UpdateResult.orThrow(): UpdateResult {
    if (!wasAcknowledged()) throw IOException()
    return this
}

fun DeleteResult.orThrow(): DeleteResult {
    if (!wasAcknowledged()) throw IOException()
    return this
}

fun BulkWriteResult.orThrow(): BulkWriteResult {
    if (!wasAcknowledged()) throw IOException()
    return this
}

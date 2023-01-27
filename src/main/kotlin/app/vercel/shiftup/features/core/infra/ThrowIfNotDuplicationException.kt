package app.vercel.shiftup.features.core.infra

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.throwIf
import com.mongodb.MongoException

fun <V, E : Throwable> Result<V, E>.throwIfNotDuplicate() = this.throwIf {
    (it is MongoException && it.code == DUPLICATE_KEY_CODE).not()
}

private const val DUPLICATE_KEY_CODE = 11000

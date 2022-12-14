package app.vercel.shiftup

import app.vercel.shiftup.presentation.firstManager
import app.vercel.shiftup.presentation.mongoDbConnectionUri
import io.ktor.server.application.*
import org.koin.dsl.module
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

val Application.kmongoModule
    get() = module {
        val mongoClient = environment.config.mongoDbConnectionUri
            ?.let(KMongo::createClient)
            ?: KMongo.createClient()
        single { mongoClient.coroutine }
        single { get<CoroutineClient>().getDatabase("shiftup") }
    }

val Application.firstManagerModule
    get() = module {
        single { environment.config.firstManager }
    }

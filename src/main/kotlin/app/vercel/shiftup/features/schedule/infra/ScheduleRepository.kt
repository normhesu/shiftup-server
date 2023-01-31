package app.vercel.shiftup.features.schedule.infra

import app.vercel.shiftup.features.core.infra.orThrow
import app.vercel.shiftup.features.schedule.domain.model.Schedule
import app.vercel.shiftup.features.schedule.domain.model.ScheduleId
import com.mongodb.client.result.DeleteResult
import org.koin.core.annotation.Single
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.updateOne
import org.litote.kmongo.upsert

@Single
class ScheduleRepository(
    database: CoroutineDatabase,
) {
    private val collection = database.getCollection<Schedule>()

    suspend fun findById(scheduleId: ScheduleId): Schedule? {
        return collection.findOneById(scheduleId)
    }

    suspend fun addOrReplace(schedule: Schedule) {
        collection.updateOne(schedule, upsert()).orThrow()
    }

    suspend fun remove(scheduleId: ScheduleId): DeleteResult {
        return collection.deleteOneById(scheduleId).orThrow()
    }
}

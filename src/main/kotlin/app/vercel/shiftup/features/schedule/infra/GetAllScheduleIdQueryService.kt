package app.vercel.shiftup.features.schedule.infra

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.schedule.domain.model.Schedule
import app.vercel.shiftup.features.schedule.domain.model.ScheduleId
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import org.bson.Document
import org.koin.core.annotation.Single
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.fields
import org.litote.kmongo.include
import kotlin.reflect.KProperty

@Single
class GetAllScheduleIdQueryService(
    database: CoroutineDatabase,
) {
    private val collection = database.getCollection<Schedule>()

    companion object {
        val scheduleIdSerializedName = Schedule::id.serializedName()
    }

    suspend operator fun invoke(): List<ScheduleId> {
        return collection.withDocumentClass<Document>()
            .find()
            .projection(fields(include(Schedule::id)))
            .toList()
            .map {
                it.getString(scheduleIdSerializedName)
                    .let(LocalDate::parse)
                    .let(::OpenCampusDate)
                    .let(::ScheduleId)
            }
    }
}

private fun KProperty<*>.serializedName(): String {
    val serialName = annotations.find { it is SerialName } as SerialName
    return serialName.value
}

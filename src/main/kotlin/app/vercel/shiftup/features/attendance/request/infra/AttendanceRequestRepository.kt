package app.vercel.shiftup.features.attendance.request.infra

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.request.domain.model.AttendanceRequest
import app.vercel.shiftup.features.attendance.request.domain.model.AttendanceRequestId
import app.vercel.shiftup.features.attendance.request.domain.model.value.AttendanceRequestState
import app.vercel.shiftup.features.attendance.request.domain.model.value.AttendanceRequestStateSerializer
import app.vercel.shiftup.features.core.infra.orThrow
import app.vercel.shiftup.features.user.account.domain.model.CastId
import com.mongodb.client.model.DeleteManyModel
import com.mongodb.client.model.InsertOneModel
import com.mongodb.client.result.DeleteResult
import org.koin.core.annotation.Single
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.updateOne
import org.litote.kmongo.eq
import org.litote.kmongo.gte
import org.litote.kmongo.`in`
import org.litote.kmongo.lt
import org.litote.kmongo.serialization.registerSerializer

@Single
class AttendanceRequestRepository(
    private val database: CoroutineDatabase,
) {
    companion object {
        init {
            registerSerializer(AttendanceRequestStateSerializer)
        }
    }

    private val collection get() = database.getCollection<AttendanceRequest>()

    suspend fun findById(attendanceRequestId: AttendanceRequestId): AttendanceRequest? {
        return collection.findOneById(attendanceRequestId)
    }

    suspend fun findByOpenCampusDate(
        openCampusDate: OpenCampusDate,
    ): List<AttendanceRequest> = collection.find(
        AttendanceRequest::openCampusDate eq openCampusDate,
    ).toList()

    suspend fun findByCastIdAndStateAndEarliestDate(
        castId: CastId,
        state: AttendanceRequestState,
        earliestDate: OpenCampusDate,
    ): List<AttendanceRequest> = collection.find(
        AttendanceRequest::castId eq castId,
        AttendanceRequest::state eq state,
        AttendanceRequest::openCampusDate gte earliestDate
    ).toList()

    suspend fun addAndRemoveAll(
        addAttendanceRequests: Collection<AttendanceRequest>,
        removeAttendanceRequests: Collection<AttendanceRequest>,
    ) {
        val insertModels = addAttendanceRequests.map(::InsertOneModel)
        val deleteManyModel = DeleteManyModel<AttendanceRequest>(
            AttendanceRequest::id `in` removeAttendanceRequests.map { it.id },
        )
        collection.bulkWrite(insertModels + deleteManyModel).orThrow()
    }

    suspend fun replace(attendanceRequest: AttendanceRequest) {
        collection.updateOne(attendanceRequest).orThrow()
    }

    suspend fun removeBeforeOpenCampusDate(openCampusDate: OpenCampusDate): DeleteResult {
        return collection.deleteMany(
            AttendanceRequest::openCampusDate lt openCampusDate,
        ).orThrow()
    }
}

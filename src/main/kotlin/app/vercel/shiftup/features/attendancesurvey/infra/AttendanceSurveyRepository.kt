package app.vercel.shiftup.features.attendancesurvey.infra

import app.vercel.shiftup.features.attendancesurvey.domain.model.AttendanceSurvey
import app.vercel.shiftup.features.attendancesurvey.domain.model.AttendanceSurveyId
import app.vercel.shiftup.features.attendancesurvey.domain.service.AttendanceSurveyRepositoryInterface
import app.vercel.shiftup.features.core.infra.orThrow
import com.mongodb.client.result.DeleteResult
import org.koin.core.annotation.Single
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.updateOne

@Single
class AttendanceSurveyRepository(
    private val database: CoroutineDatabase,
) : AttendanceSurveyRepositoryInterface {
    private val collection get() = database.getCollection<AttendanceSurvey>()

    override suspend fun findById(attendanceSurveyId: AttendanceSurveyId): AttendanceSurvey? {
        return collection.findOneById(attendanceSurveyId)
    }

    suspend fun findAll(): List<AttendanceSurvey> {
        return collection.find().toList()
    }

    suspend fun addSurvey(survey: AttendanceSurvey) {
        collection.insertOne(survey).orThrow()
    }

    suspend fun replace(survey: AttendanceSurvey) {
        collection.updateOne(survey).orThrow()
    }

    suspend fun remove(surveyId: AttendanceSurveyId): DeleteResult {
        return collection.deleteOneById(surveyId).orThrow()
    }
}

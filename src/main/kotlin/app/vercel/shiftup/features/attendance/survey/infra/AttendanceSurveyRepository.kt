package app.vercel.shiftup.features.attendance.survey.infra

import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurvey
import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurveyId
import app.vercel.shiftup.features.attendance.survey.domain.service.AttendanceSurveyRepositoryInterface
import app.vercel.shiftup.features.core.infra.orThrow
import com.mongodb.client.result.DeleteResult
import org.koin.core.annotation.Single
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.updateOne
import org.litote.kmongo.`in`

@Single
class AttendanceSurveyRepository(
    private val database: CoroutineDatabase,
) : AttendanceSurveyRepositoryInterface {
    private val collection get() = database.getCollection<AttendanceSurvey>()

    override suspend fun findById(attendanceSurveyId: AttendanceSurveyId): AttendanceSurvey? {
        return collection.findOneById(attendanceSurveyId)
    }

    override suspend fun findAll(): List<AttendanceSurvey> {
        return collection.find().toList()
    }

    suspend fun add(survey: AttendanceSurvey) {
        collection.insertOne(survey).orThrow()
    }

    suspend fun replace(survey: AttendanceSurvey) {
        collection.updateOne(survey).orThrow()
    }

    suspend fun remove(surveyId: AttendanceSurveyId): DeleteResult {
        return collection.deleteOneById(surveyId).orThrow()
    }

    suspend fun removeAllById(surveyIds: Collection<AttendanceSurveyId>): DeleteResult {
        return collection.deleteMany(AttendanceSurvey::id `in` surveyIds)
    }
}

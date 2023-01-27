package app.vercel.shiftup.features.attendance.survey.infra

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurvey
import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurveyId
import app.vercel.shiftup.features.attendance.survey.domain.service.AttendanceSurveyRepositoryInterface
import app.vercel.shiftup.features.core.infra.orThrow
import com.mongodb.client.model.Filters
import com.mongodb.client.result.DeleteResult
import org.koin.core.annotation.Single
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.updateOne
import org.litote.kmongo.`in`
import org.litote.kmongo.nin
import org.litote.kmongo.path

@Single
class AttendanceSurveyRepository(
    database: CoroutineDatabase,
) : AttendanceSurveyRepositoryInterface {
    private val collection = database.getCollection<AttendanceSurvey>()

    override suspend fun findById(attendanceSurveyId: AttendanceSurveyId): AttendanceSurvey? {
        return collection.findOneById(attendanceSurveyId)
    }

    override suspend fun findAll(): List<AttendanceSurvey> {
        return collection.find().toList()
    }

    suspend fun findByOpenCampusDates(
        openCampusDates: Set<OpenCampusDate>,
    ) = collection
        .find(Filters.`in`(AttendanceSurvey::openCampusSchedule.path(), openCampusDates))
        .toList()

    suspend fun countByNotContainsIds(ids: Collection<AttendanceSurveyId>): Long {
        return collection.countDocuments(AttendanceSurvey::id nin ids)
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

package app.vercel.shiftup.features.attendancesurvey.answer.infra

import app.vercel.shiftup.features.attendancesurvey.answer.domain.model.AttendanceSurveyAnswer
import app.vercel.shiftup.features.attendancesurvey.domain.model.value.AttendanceSurveyId
import app.vercel.shiftup.features.attendancesurvey.survey.domain.model.value.AttendanceSurveyAnswers
import app.vercel.shiftup.features.core.infra.orThrow
import com.mongodb.client.result.DeleteResult
import org.koin.core.annotation.Single
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.updateOne
import org.litote.kmongo.eq
import org.litote.kmongo.upsert

@Single
class AttendanceSurveyAnswerRepository(
    private val database: CoroutineDatabase,
) {
    private val collection get() = database.getCollection<AttendanceSurveyAnswer>()

    suspend fun addOrReplace(answer: AttendanceSurveyAnswer) {
        collection.updateOne(answer, upsert()).orThrow()
    }

    suspend fun findBySurveyId(
        surveyId: AttendanceSurveyId,
    ) = collection
        .find(AttendanceSurveyAnswer::surveyId eq surveyId)
        .toList()
        .toSet()
        .let(::AttendanceSurveyAnswers)

    suspend fun removeBySurveyId(surveyId: AttendanceSurveyId): DeleteResult {
        return collection.deleteMany(AttendanceSurveyAnswer::surveyId eq surveyId).orThrow()
    }
}

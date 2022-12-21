package app.vercel.shiftup.features.attendance.survey.answer.infra

import app.vercel.shiftup.features.attendance.survey.answer.domain.model.AttendanceSurveyAnswer
import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurveyId
import app.vercel.shiftup.features.attendance.survey.domain.model.value.AttendanceSurveyAnswers
import app.vercel.shiftup.features.core.infra.orThrow
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.updateOne

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
        .let {
            AttendanceSurveyAnswers(answers = it, surveyId = surveyId)
        }

    suspend fun countBySurveyIds(
        surveyIds: Collection<AttendanceSurveyId>,
    ): Map<AttendanceSurveyId, Int> {
        @Serializable
        data class CountResult(
            val surveyId: AttendanceSurveyId,
            val count: Int,
        )

        val aggregateResult = collection.aggregate<CountResult>(
            listOf(
                match(AttendanceSurveyAnswer::surveyId `in` surveyIds),
                group(
                    id = AttendanceSurveyAnswer::surveyId,
                    fieldAccumulators = listOf(
                        CountResult::surveyId first AttendanceSurveyAnswer::surveyId,
                        CountResult::count.count,
                    ),
                )
            ),
        ).toList()

        val noAnswerSurveyIds = surveyIds - aggregateResult.map { it.surveyId }.toSet()
        val counts = aggregateResult + noAnswerSurveyIds.map {
            CountResult(
                surveyId = it,
                count = 0,
            )
        }

        return counts.associate { it.surveyId to it.count }
    }
}

package app.vercel.shiftup.features.attendance.survey.domain.model.value

import app.vercel.shiftup.features.attendance.survey.answer.domain.model.AttendanceSurveyAnswer
import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurveyId
import kotlinx.serialization.Serializable

@Serializable
data class AttendanceSurveyAnswers(
    private val answers: Set<AttendanceSurveyAnswer>,
    private val surveyId: AttendanceSurveyId
) {
    companion object {
        fun empty(surveyId: AttendanceSurveyId) = AttendanceSurveyAnswers(
            answers = emptySet(),
            surveyId = surveyId,
        )
    }

    init {
        require(answers.all { it.surveyId == surveyId })
    }

    fun <T> fold(
        initial: T,
        operation: (acc: T, AttendanceSurveyAnswer) -> T,
    ): T = answers.fold(
        initial, operation,
    )
}

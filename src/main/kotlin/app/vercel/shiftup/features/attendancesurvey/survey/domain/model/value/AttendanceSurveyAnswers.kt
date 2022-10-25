package app.vercel.shiftup.features.attendancesurvey.survey.domain.model.value

import app.vercel.shiftup.features.attendancesurvey.answer.domain.model.AttendanceSurveyAnswer
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class AttendanceSurveyAnswers(private val value: Set<AttendanceSurveyAnswer>) {
    init {
        require(value.all { it.surveyId == surveyIdOrNull })
    }

    val isEmpty get() = value.isEmpty()
    val surveyIdOrNull get() = value.firstOrNull()?.surveyId

    fun <T> fold(
        initial: T,
        operation: (acc: T, AttendanceSurveyAnswer) -> T,
    ): T = value.fold(
        initial, operation,
    )
}

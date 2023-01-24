package app.vercel.shiftup.features.attendance.survey.answer.domain.model

import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurveyId
import app.vercel.shiftup.features.attendance.survey.domain.model.value.SameFiscalYearOpenCampusDates
import app.vercel.shiftup.features.user.account.domain.model.CastId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class AttendanceSurveyAnswerId(
    val attendanceSurveyId: AttendanceSurveyId,
    val availableCastId: CastId,
)

@Serializable
@Suppress("DataClassPrivateConstructor")
data class AttendanceSurveyAnswer private constructor(
    val surveyId: AttendanceSurveyId,
    val availableCastId: CastId,
    val availableDays: SameFiscalYearOpenCampusDates,
    @SerialName("_id") val id: AttendanceSurveyAnswerId,
) {
    companion object {
        fun fromFactory(
            surveyId: AttendanceSurveyId,
            availableCastId: CastId,
            availableDays: SameFiscalYearOpenCampusDates,
        ) = AttendanceSurveyAnswer(
            surveyId = surveyId,
            availableCastId = availableCastId,
            availableDays = availableDays,
            id = AttendanceSurveyAnswerId(
                attendanceSurveyId = surveyId,
                availableCastId = availableCastId,
            ),
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AttendanceSurveyAnswer
        return id == other.id
    }

    override fun hashCode() = id.hashCode()
}

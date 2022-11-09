package app.vercel.shiftup.features.attendancesurvey.domain.model.value

import app.vercel.shiftup.features.attendancesurvey.domain.model.AttendanceSurveyId
import app.vercel.shiftup.features.user.account.domain.model.CastId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
@JvmInline
value class AttendanceSurveyAnswerId(
    @Suppress("unused")
    private val value: Pair<AttendanceSurveyId, CastId>,
)

@Serializable
@Suppress("DataClassPrivateConstructor")
data class AttendanceSurveyAnswer private constructor(
    val surveyId: AttendanceSurveyId,
    val availableCastId: CastId,
    val availableDays: OpenCampusDates,
    @SerialName("_id") val id: AttendanceSurveyAnswerId,
) {
    companion object {
        fun fromFactory(
            surveyId: AttendanceSurveyId,
            availableCastId: CastId,
            availableDays: OpenCampusDates,
        ) = AttendanceSurveyAnswer(
            surveyId = surveyId,
            availableCastId = availableCastId,
            availableDays = availableDays,
            id = AttendanceSurveyAnswerId(
                surveyId to availableCastId,
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

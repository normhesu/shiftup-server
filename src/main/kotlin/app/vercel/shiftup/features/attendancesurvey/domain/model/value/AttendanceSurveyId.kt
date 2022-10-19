package app.vercel.shiftup.features.attendancesurvey.domain.model.value

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
@JvmInline
value class AttendanceSurveyId(
    @Suppress("unused")
    private val value: String = UUID.randomUUID().toString(),
)

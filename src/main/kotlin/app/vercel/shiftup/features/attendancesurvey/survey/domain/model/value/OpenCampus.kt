package app.vercel.shiftup.features.attendancesurvey.survey.domain.model.value

import app.vercel.shiftup.features.attendancesurvey.answer.domain.model.AttendanceSurveyAnswer
import app.vercel.shiftup.features.attendancesurvey.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.user.account.domain.model.CastId
import kotlinx.serialization.Serializable

@Serializable
@Suppress("DataClassPrivateConstructor")
data class OpenCampus private constructor(
    val date: OpenCampusDate,
    val availableCastIds: Set<CastId>,
) {

    constructor(date: OpenCampusDate) : this(
        date = date,
        availableCastIds = emptySet(),
    )

    fun addAvailableCastOrNothing(
        answer: AttendanceSurveyAnswer,
    ) = when (date) {
        in answer.availableDays -> addAvailableCastId(
            answer.availableCastId
        )
        else -> this
    }

    private fun addAvailableCastId(castId: CastId) = copy(
        availableCastIds = availableCastIds.toMutableSet().apply {
            add(castId)
        }
    )
}

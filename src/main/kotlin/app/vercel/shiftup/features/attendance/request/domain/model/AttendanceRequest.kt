package app.vercel.shiftup.features.attendance.request.domain.model

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.request.domain.model.value.AttendanceRequestState
import app.vercel.shiftup.features.user.account.domain.model.AvailableUser
import app.vercel.shiftup.features.user.account.domain.model.CastId
import app.vercel.shiftup.features.user.domain.model.value.Role
import com.github.michaelbull.result.runCatching
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AttendanceRequestId(
    val openCampusDate: OpenCampusDate,
    val castId: CastId,
)

@Suppress("DataClassPrivateConstructor")
@Serializable
data class AttendanceRequest private constructor(
    val openCampusDate: OpenCampusDate,
    val castId: CastId,
    val state: AttendanceRequestState,
    @SerialName("_id") val id: AttendanceRequestId,
) {
    companion object {
        operator fun invoke(
            openCampusDate: OpenCampusDate,
            castId: CastId,
        ) = AttendanceRequest(
            openCampusDate = openCampusDate,
            castId = castId,
            state = AttendanceRequestState.Blank,
            id = AttendanceRequestId(
                openCampusDate = openCampusDate,
                castId = castId,
            )
        )
    }

    fun respond(newState: AttendanceRequestState.NonBlank) = runCatching {
        check(state == AttendanceRequestState.Blank)
        copy(state = newState)
    }

    fun forcedChangeState(state: AttendanceRequestState, operator: AvailableUser): AttendanceRequest {
        require(operator.hasRole(Role.Manager))
        return copy(state = state)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AttendanceRequest
        return id == other.id
    }

    override fun hashCode() = id.hashCode()
}

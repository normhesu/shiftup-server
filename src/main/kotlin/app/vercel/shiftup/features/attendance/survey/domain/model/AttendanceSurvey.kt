package app.vercel.shiftup.features.attendance.survey.domain.model

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.survey.domain.model.value.*
import app.vercel.shiftup.features.user.account.domain.model.Cast
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
@JvmInline
value class AttendanceSurveyId(
    @Suppress("unused")
    private val value: String = UUID.randomUUID().toString(),
)

@Serializable
@Suppress("DataClassPrivateConstructor")
data class AttendanceSurvey private constructor(
    val name: String,
    val openCampusSchedule: OpenCampusDates,
    val creationDate: LocalDate,
    val available: Boolean,
    @SerialName("_id") val id: AttendanceSurveyId,
) {
    companion object {
        fun fromFactory(
            name: String,
            openCampusSchedule: OpenCampusDates,
            creationDate: LocalDate,
            available: Boolean,
            id: AttendanceSurveyId,
        ) = AttendanceSurvey(
            name = name,
            openCampusSchedule = openCampusSchedule,
            creationDate = creationDate,
            available = available,
            id = id,
        )
    }

    init {
        require(name.isNotBlank())
        require(openCampusSchedule.isNotEmpty())
        require(OpenCampusDate(creationDate) <= openCampusSchedule.earliestDateOrThrow()) {
            "全てのオープンキャンパスの日程は、アンケート作成日以降にする必要があります"
        }
    }

    val fiscalYear = openCampusSchedule.fiscalYear

    fun changeAvailable(available: Boolean) = copy(available = available)

    fun tally(
        answers: AttendanceSurveyAnswers,
    ): Set<OpenCampus> = answers.fold(
        openCampusSchedule.sinceNow().map(::OpenCampus)
    ) { openCampuses, answer ->
        openCampuses.map {
            it.addAvailableCastOrNothing(answer)
        }
    }.toSet()

    fun canAnswer(cast: Cast): Boolean {
        return available && !isAfterOpenCampusPeriod() && cast.inSchool(fiscalYear)
    }

    fun canSendAttendanceRequest(openCampusDate: OpenCampusDate? = null): Boolean {
        return openCampusSchedule.laterDateOrThrow() >= (openCampusDate ?: OpenCampusDate.now())
    }

    fun isAfterOpenCampusPeriod(openCampusDate: OpenCampusDate? = null): Boolean {
        return openCampusSchedule.laterDateOrThrow() < (openCampusDate ?: OpenCampusDate.now())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AttendanceSurvey
        return id == other.id
    }

    override fun hashCode() = id.hashCode()
}

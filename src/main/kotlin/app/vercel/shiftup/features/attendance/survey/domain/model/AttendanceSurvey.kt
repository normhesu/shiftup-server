package app.vercel.shiftup.features.attendance.survey.domain.model

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.survey.domain.model.value.*
import app.vercel.shiftup.features.core.domain.model.toTokyoLocalDateTime
import kotlinx.datetime.Clock
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
    val answers: AttendanceSurveyAnswers,
    val creationDate: LocalDate,
    val available: Boolean,
    @SerialName("_id") val id: AttendanceSurveyId,
) {
    companion object {
        operator fun invoke(
            name: String,
            openCampusSchedule: OpenCampusDates,
        ): AttendanceSurvey {
            val id = AttendanceSurveyId()
            return AttendanceSurvey(
                name = name,
                openCampusSchedule = openCampusSchedule,
                answers = AttendanceSurveyAnswers.empty(id),
                creationDate = Clock.System.now().toTokyoLocalDateTime().date,
                available = true,
                id = id,
            )
        }
    }

    init {
        require(name.isNotBlank())
        require(openCampusSchedule.isNotEmpty())
        require(OpenCampusDate(creationDate) <= openCampusSchedule.earliestDateOrThrow()) {
            "全てのオープンキャンパスの日程は、現在の日にち以降にする必要があります"
        }
    }

    val fiscalYear = openCampusSchedule.fiscalYear

    fun changeAvailable(available: Boolean) = copy(available = available)

    fun addOrReplaceAnswer(answer: AttendanceSurveyAnswer) = copy(
        answers = answers.addOrReplace(answer)
    )

    fun tally(): Set<OpenCampus> = answers.fold(
        openCampusSchedule.map(::OpenCampus)
    ) { openCampuses, answer ->
        openCampuses.map {
            it.addAvailableCastOrNothing(answer)
        }
    }.toSet()

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
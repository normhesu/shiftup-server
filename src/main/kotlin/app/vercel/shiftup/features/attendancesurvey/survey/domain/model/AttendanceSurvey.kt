package app.vercel.shiftup.features.attendancesurvey.survey.domain.model

import app.vercel.shiftup.features.attendancesurvey.domain.model.value.AttendanceSurveyId
import app.vercel.shiftup.features.attendancesurvey.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendancesurvey.domain.model.value.OpenCampusDates
import app.vercel.shiftup.features.attendancesurvey.survey.domain.model.value.AttendanceSurveyAnswers
import app.vercel.shiftup.features.attendancesurvey.survey.domain.model.value.OpenCampus
import app.vercel.shiftup.features.core.domain.model.nowTokyoLocalDateTime
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Suppress("DataClassPrivateConstructor")
data class AttendanceSurvey private constructor(
    val name: String,
    val openCampusSchedule: OpenCampusDates,
    val creationDate: LocalDate,
    val isAvailable: Boolean,
    @SerialName("_id") val id: AttendanceSurveyId,
) {
    companion object {
        fun of(
            name: String,
            openCampusSchedule: OpenCampusDates,
        ) = AttendanceSurvey(
            name = name,
            openCampusSchedule = openCampusSchedule,
            creationDate = Clock.System.nowTokyoLocalDateTime().date,
            isAvailable = true,
            id = AttendanceSurveyId(),
        )
    }

    init {
        require(name.isNotBlank())
        require(openCampusSchedule.isNotEmpty())
        require(OpenCampusDate(creationDate) <= openCampusSchedule.earliestDateOrThrow()) {
            "全てのオープンキャンパスの日程は、現在の日にち以降にする必要があります"
        }
    }

    val fiscalYear = openCampusSchedule.fiscalYear

    fun changeAvailable(available: Boolean) = copy(isAvailable = available)

    fun tally(
        answers: AttendanceSurveyAnswers,
    ): Set<OpenCampus> {
        require(answers.isEmpty || answers.surveyIdOrNull == id)
        return answers.fold(
            openCampusSchedule.map(::OpenCampus)
        ) { openCampuses, answer ->
            openCampuses.map {
                it.addAvailableCastOrNothing(answer)
            }
        }.toSet()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AttendanceSurvey
        return id == other.id
    }

    override fun hashCode() = id.hashCode()
}

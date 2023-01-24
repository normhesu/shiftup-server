package app.vercel.shiftup.features.attendance.survey.domain.service

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.request.domain.service.AttendanceRequestRepositoryInterface
import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurvey
import app.vercel.shiftup.features.attendance.survey.domain.model.value.AttendanceSurveyAnswers
import app.vercel.shiftup.features.user.account.domain.model.Cast
import app.vercel.shiftup.features.user.domain.model.value.Department
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single

@Single
class TallyAttendanceSurveyDomainService(
    private val attendanceRequestRepository: AttendanceRequestRepositoryInterface,
    private val getCastsByCastIds: GetCastsByCastIdsApplicationServiceInterface,
) {
    suspend operator fun invoke(
        survey: AttendanceSurvey,
        answers: AttendanceSurveyAnswers,
    ): TallyAttendanceSurveyResult = coroutineScope {
        val openCampuses = survey.tally(answers)
        val (attendanceRequests, casts) = run {
            val availableCastIds = openCampuses.map { it.availableCastIds }
                .flatten()
                .toSet()

            val attendanceRequestsDeferred = async {
                attendanceRequestRepository
                    .findByCastIds(availableCastIds)
                    .groupBy { it.openCampusDate }
            }
            val castsDeferred = async {
                getCastsByCastIds(availableCastIds).sortedWith(
                    compareBy<Cast> {
                        Department.values.indexOf(it.value.department)
                    }.thenBy {
                        it.value.studentNumber
                    }
                ).toSet()
            }

            attendanceRequestsDeferred.await() to castsDeferred.await()
        }

        val resultItems = openCampuses.map { openCampus ->
            fun Cast.attendanceRequested() = attendanceRequests
                .filterKeys { it == openCampus.date }
                .any { (_, attendanceRequests) ->
                    attendanceRequests.find { it.castId == this.id } != null
                }

            val availableCastsWithAttendanceRequested = casts
                .filter { it.id in openCampus.availableCastIds }
                .map {
                    CastWithAttendanceRequested(
                        cast = it,
                        attendanceRequested = it.attendanceRequested()
                    )
                }
                .toSet()

            TallyAttendanceSurveyResultItem(
                openCampusDate = openCampus.date,
                castsWithAttendanceRequested = availableCastsWithAttendanceRequested,
                tallied = availableCastsWithAttendanceRequested.any { it.attendanceRequested },
            )
        }.toSet()

        TallyAttendanceSurveyResult(
            results = resultItems,
            tallied = resultItems.all { it.tallied },
        )
    }
}

@Serializable
data class TallyAttendanceSurveyResult(
    val tallied: Boolean,
    val results: Set<TallyAttendanceSurveyResultItem>,
)

@Serializable
data class TallyAttendanceSurveyResultItem(
    val openCampusDate: OpenCampusDate,
    val tallied: Boolean,
    val castsWithAttendanceRequested: Set<CastWithAttendanceRequested>,
)

@Serializable
data class CastWithAttendanceRequested(
    val cast: Cast,
    val attendanceRequested: Boolean,
)

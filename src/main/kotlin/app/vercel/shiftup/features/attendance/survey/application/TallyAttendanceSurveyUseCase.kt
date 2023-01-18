package app.vercel.shiftup.features.attendance.survey.application

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.request.infra.AttendanceRequestRepository
import app.vercel.shiftup.features.attendance.survey.answer.infra.AttendanceSurveyAnswerRepository
import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurveyId
import app.vercel.shiftup.features.attendance.survey.domain.service.AttendanceSurveyRepositoryInterface
import app.vercel.shiftup.features.user.account.application.service.GetCastsByCastIdsApplicationService
import app.vercel.shiftup.features.user.account.domain.model.Cast
import app.vercel.shiftup.features.user.account.domain.model.CastId
import app.vercel.shiftup.features.user.domain.model.value.Department
import io.ktor.server.plugins.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single

@Single
class TallyAttendanceSurveyUseCase(
    private val attendanceSurveyRepository: AttendanceSurveyRepositoryInterface,
    private val attendanceSurveyAnswerRepository: AttendanceSurveyAnswerRepository,
    private val attendanceRequestRepository: AttendanceRequestRepository,
    private val getCastsByCastIdsApplicationService: GetCastsByCastIdsApplicationService,
) {
    suspend operator fun invoke(
        surveyId: AttendanceSurveyId,
    ): TallyAttendanceSurveyUseCaseResult = coroutineScope {
        val openCampuses = getAnsweredOpenCampuses(surveyId)
        val (attendanceRequests, casts) = run {
            val availableCastIds = openCampuses.map { it.availableCastIds }
                .flatten()
                .toSet()

            val attendanceRequestsDeferred = async { getAttendanceRequests(availableCastIds) }
            val castsDeferred = async { getCasts(availableCastIds) }

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

            TallyAttendanceSurveyUseCaseResultItem(
                openCampusDate = openCampus.date,
                castsWithAttendanceRequested = availableCastsWithAttendanceRequested,
                tallied = availableCastsWithAttendanceRequested.any { it.attendanceRequested },
            )
        }.toSet()

        TallyAttendanceSurveyUseCaseResult(
            results = resultItems,
            tallied = resultItems.all { it.tallied },
        )
    }

    private suspend fun getAnsweredOpenCampuses(surveyId: AttendanceSurveyId) = coroutineScope {
        val surveyDeferred = async { attendanceSurveyRepository.findById(surveyId) ?: throw NotFoundException() }
        val answersDeferred = async { attendanceSurveyAnswerRepository.findBySurveyId(surveyId) }

        surveyDeferred.await().tally(
            answersDeferred.await()
        )
    }

    private suspend fun getAttendanceRequests(
        availableCastIds: Set<CastId>,
    ) = attendanceRequestRepository.findByCastIds(availableCastIds).groupBy {
        it.openCampusDate
    }

    private suspend fun getCasts(
        availableCastIds: Set<CastId>,
    ) = getCastsByCastIdsApplicationService(availableCastIds)
        .sortedWith(
            compareBy<Cast> {
                Department.values.indexOf(it.value.department)
            }.thenBy {
                it.value.studentNumber
            }
        ).toSet()
}

@Serializable
data class TallyAttendanceSurveyUseCaseResult(
    val tallied: Boolean,
    val results: Set<TallyAttendanceSurveyUseCaseResultItem>,
)

@Serializable
data class TallyAttendanceSurveyUseCaseResultItem(
    val openCampusDate: OpenCampusDate,
    val tallied: Boolean,
    val castsWithAttendanceRequested: Set<CastWithAttendanceRequested>,
)

@Serializable
data class CastWithAttendanceRequested(
    val cast: Cast,
    val attendanceRequested: Boolean,
)

package app.vercel.shiftup.features.user.account.application

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.request.domain.model.value.AttendanceRequestState
import app.vercel.shiftup.features.attendance.request.infra.AttendanceRequestRepository
import app.vercel.shiftup.features.attendance.survey.answer.infra.AttendanceSurveyAnswerRepository
import app.vercel.shiftup.features.attendance.survey.infra.AttendanceSurveyRepository
import app.vercel.shiftup.features.user.account.application.service.GetCastApplicationService
import app.vercel.shiftup.features.user.account.domain.model.CastId
import app.vercel.shiftup.features.user.account.domain.model.UserId
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single

@Single
class GetAvailableUserDetailUseCase(
    private val attendanceSurveyRepository: AttendanceSurveyRepository,
    private val attendanceSurveyAnswerRepository: AttendanceSurveyAnswerRepository,
    private val attendanceRequestRepository: AttendanceRequestRepository,
    private val getCastApplicationService: GetCastApplicationService,
) {
    suspend operator fun invoke(userId: UserId): GetAvailableUserDetailUseCaseResult = coroutineScope {
        val unSafeCastId = CastId.unsafe(userId)

        val castDeferred = async {
            getCastApplicationService(userId)
        }
        val unansweredAttendanceSurveyCountDeferred = async {
            getUnansweredAttendanceSurveyCount(unSafeCastId)
        }
        val blankAttendanceRequestCountDeferred = async {
            getCanRespondAttendanceRequestCount(unSafeCastId)
        }
        val nextWorkDayDeferred = async {
            getNextWorkDay(unSafeCastId)
        }
        val currentMonthWorkedDayCountDeferred = async {
            getCurrentMonthWorkedDayCount(unSafeCastId)
        }
        val currentMonthWorkScheduleDayCountDeferred = async {
            getCurrentMonthWorkScheduleDayCount(unSafeCastId)
        }

        castDeferred.await()
        GetAvailableUserDetailUseCaseResult(
            unansweredAttendanceSurveyCount = unansweredAttendanceSurveyCountDeferred.await(),
            canRespondAttendanceRequestCount = blankAttendanceRequestCountDeferred.await(),
            nextWorkDay = nextWorkDayDeferred.await(),
            currentMonthWorkedDayCount = currentMonthWorkedDayCountDeferred.await(),
            currentMonthWorkScheduleDayCount = currentMonthWorkScheduleDayCountDeferred.await(),
        )
    }

    private suspend fun getUnansweredAttendanceSurveyCount(
        castId: CastId,
    ): Long {
        val answers = attendanceSurveyAnswerRepository.findByCastId(castId)
        return attendanceSurveyRepository.countByNotContainsIds(
            answers.map { it.surveyId }
        )
    }

    private suspend fun getCanRespondAttendanceRequestCount(
        castId: CastId,
    ): Long = attendanceRequestRepository.countByCastIdAndStateAndEarliestDate(
        castId = castId,
        state = AttendanceRequestState.Blank,
        earliestDate = OpenCampusDate.now(),
    )

    private suspend fun getNextWorkDay(castId: CastId): OpenCampusDate? {
        val request = attendanceRequestRepository.findOldestOpenCampusDateRequestByCastIdAndStateAndAfterDate(
            castId = castId,
            state = AttendanceRequestState.Accepted,
            openCampusDate = OpenCampusDate.now(),
        )
        return request?.openCampusDate
    }

    private suspend fun getCurrentMonthWorkedDayCount(
        castId: CastId,
    ): Int = attendanceRequestRepository.countByCastIdAndStateAndOpenCampusDuration(
        castId = castId,
        state = AttendanceRequestState.Accepted,
        startOpenCampusDate = OpenCampusDate.firstDayOfThisMonth(),
        endOpenCampusDate = OpenCampusDate.now().previousDay(),
    ).toInt()

    private suspend fun getCurrentMonthWorkScheduleDayCount(
        castId: CastId,
    ): Int = attendanceRequestRepository.countByCastIdAndStateAndOpenCampusDuration(
        castId = castId,
        state = AttendanceRequestState.Accepted,
        startOpenCampusDate = OpenCampusDate.now(),
        endOpenCampusDate = OpenCampusDate.lastDayOfThisMonth(),
    ).toInt()
}

@Serializable
data class GetAvailableUserDetailUseCaseResult(
    val unansweredAttendanceSurveyCount: Long,
    val canRespondAttendanceRequestCount: Long,
    val nextWorkDay: OpenCampusDate?,
    val currentMonthWorkedDayCount: Int,
    val currentMonthWorkScheduleDayCount: Int,
)

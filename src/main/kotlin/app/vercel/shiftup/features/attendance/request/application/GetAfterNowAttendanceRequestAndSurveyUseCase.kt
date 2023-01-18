package app.vercel.shiftup.features.attendance.request.application

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.request.domain.model.AttendanceRequest
import app.vercel.shiftup.features.attendance.request.infra.AttendanceRequestRepository
import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurvey
import app.vercel.shiftup.features.attendance.survey.infra.AttendanceSurveyRepository
import app.vercel.shiftup.features.user.account.application.service.GetCastApplicationService
import app.vercel.shiftup.features.user.account.domain.model.CastId
import app.vercel.shiftup.features.user.account.domain.model.UserId
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

@Single
class GetAfterNowAttendanceRequestAndSurveyUseCase(
    private val attendanceRequestRepository: AttendanceRequestRepository,
    private val attendanceSurveyRepository: AttendanceSurveyRepository,
    private val getCastApplicationService: GetCastApplicationService,
) {
    suspend operator fun invoke(
        userId: UserId,
    ): GetAfterNowAttendanceRequestAndSurveyUseCaseResult = coroutineScope {
        launch { getCastApplicationService(userId) }
        val requests = attendanceRequestRepository.findByCastIdAndEarliestDate(
            castId = CastId.unsafe(userId),
            earliestDate = OpenCampusDate.now(),
        ).sortedBy {
            it.openCampusDate
        }
        val surveys = attendanceSurveyRepository.findByOpenCampusDates(
            openCampusDates = requests.map { it.openCampusDate }.toSet()
        ).map { survey ->
            survey.openCampusSchedule.associateWith { survey }.toList()
        }.flatten().toMap()

        val requestAndSurveyList = requests.map {
            it to surveys[it.openCampusDate]
        }

        val (
            canRespondRequestAndSurveyList,
            respondedRequestAndSurveyList,
        ) = requestAndSurveyList.partition { (request, _) ->
            request.canRespond
        }

        GetAfterNowAttendanceRequestAndSurveyUseCaseResult(
            canRespondRequestAndSurveyList = canRespondRequestAndSurveyList,
            respondedRequestAndSurveyList = respondedRequestAndSurveyList
        )
    }
}

data class GetAfterNowAttendanceRequestAndSurveyUseCaseResult(
    val canRespondRequestAndSurveyList: List<Pair<AttendanceRequest, AttendanceSurvey?>>,
    val respondedRequestAndSurveyList: List<Pair<AttendanceRequest, AttendanceSurvey?>>,
)

package app.vercel.shiftup.features.attendance.request.application

import app.vercel.shiftup.features.attendance.application.removeAttendanceSurveyUseCaseAndApplyAttendanceRequestToOpenCampusDateUseCaseMutex
import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.request.domain.model.AttendanceRequest
import app.vercel.shiftup.features.attendance.request.infra.AttendanceRequestRepository
import app.vercel.shiftup.features.user.account.application.service.GetCastsByCastIdsApplicationService
import app.vercel.shiftup.features.user.account.domain.model.CastId
import app.vercel.shiftup.features.user.account.domain.model.UserId
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single

@Single
class ApplyAttendanceRequestToOpenCampusDateUseCase(
    private val attendanceRequestRepository: AttendanceRequestRepository,
    private val getCastsByCastIdsApplicationService: GetCastsByCastIdsApplicationService,
) {
    suspend operator fun invoke(
        openCampusDate: OpenCampusDate,
        userIds: Set<UserId>,
    ) = removeAttendanceSurveyUseCaseAndApplyAttendanceRequestToOpenCampusDateUseCaseMutex.withLock {
        coroutineScope {
            require(openCampusDate >= OpenCampusDate.now())

            val castsDeferred = async {
                getCastsByCastIdsApplicationService(userIds.map(CastId::unsafe))
            }
            val currentRequestsDeferred = async {
                attendanceRequestRepository.findByOpenCampusDate(openCampusDate).toSet()
            }

            val applyRequests = userIds.map {
                AttendanceRequest(castId = CastId.unsafe(it), openCampusDate = openCampusDate)
            }.toSet()
            val currentRequests = currentRequestsDeferred.await()
            castsDeferred.await()

            attendanceRequestRepository.addAndRemoveAll(
                addAttendanceRequests = applyRequests - currentRequests,
                removeAttendanceRequests = currentRequests - applyRequests,
            )
        }
    }
}

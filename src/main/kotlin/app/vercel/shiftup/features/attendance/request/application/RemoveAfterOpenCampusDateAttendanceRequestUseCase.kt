package app.vercel.shiftup.features.attendance.request.application

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.request.infra.AttendanceRequestRepository
import com.mongodb.client.result.DeleteResult
import org.koin.core.annotation.Single

@Single
class RemoveAfterOpenCampusDateAttendanceRequestUseCase(
    private val attendanceRequestRepository: AttendanceRequestRepository,
) {
    suspend operator fun invoke(): DeleteResult {
        return attendanceRequestRepository.removeBeforeOpenCampusDate(OpenCampusDate.now())
    }
}

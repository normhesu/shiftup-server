package app.vercel.shiftup.features.attendance.request.domain.service

import app.vercel.shiftup.features.attendance.request.domain.model.AttendanceRequest
import app.vercel.shiftup.features.user.account.domain.model.CastId

interface AttendanceRequestRepositoryInterface {
    suspend fun findByCastIds(castIds: Collection<CastId>): List<AttendanceRequest>
}

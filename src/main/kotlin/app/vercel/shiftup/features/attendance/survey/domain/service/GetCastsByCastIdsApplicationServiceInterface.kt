package app.vercel.shiftup.features.attendance.survey.domain.service

import app.vercel.shiftup.features.user.account.domain.model.Cast
import app.vercel.shiftup.features.user.account.domain.model.CastId

interface GetCastsByCastIdsApplicationServiceInterface {
    suspend operator fun invoke(castIds: Iterable<CastId>): List<Cast>
}

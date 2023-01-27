package app.vercel.shiftup.features.user.account.application.service

import app.vercel.shiftup.features.attendance.survey.domain.service.GetCastsByCastIdsApplicationServiceInterface
import app.vercel.shiftup.features.user.account.domain.model.Cast
import app.vercel.shiftup.features.user.account.domain.model.CastId
import app.vercel.shiftup.features.user.account.infra.AvailableUserRepository
import org.koin.core.annotation.Single

@Single
class GetCastsByCastIdsApplicationService(
    private val availableUserRepository: AvailableUserRepository,
) : GetCastsByCastIdsApplicationServiceInterface {
    override suspend operator fun invoke(castIds: Iterable<CastId>): List<Cast> {
        val users = availableUserRepository.findByIds(castIds.map { it.value })
        // ユーザーのロールが変更されてCastの生成に失敗した場合は返さない
        return users.mapNotNull {
            runCatching { Cast(it) }.getOrNull()
        }
    }
}

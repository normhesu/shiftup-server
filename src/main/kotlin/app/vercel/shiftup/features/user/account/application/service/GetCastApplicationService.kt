package app.vercel.shiftup.features.user.account.application.service

import app.vercel.shiftup.features.user.account.domain.model.Cast
import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.account.infra.AvailableUserRepository
import org.koin.core.annotation.Single

@Single
class GetCastApplicationService(
    private val availableUserRepository: AvailableUserRepository,
) {
    suspend operator fun invoke(
        userId: UserId,
    ) = availableUserRepository.findById(userId)
        .let(::checkNotNull)
        .let(::Cast)
}

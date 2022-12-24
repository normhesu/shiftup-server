package app.vercel.shiftup.features.user.account.application.service

import app.vercel.shiftup.features.user.account.domain.model.Cast
import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.account.infra.UserRepository
import org.koin.core.annotation.Single

@Single
class GetCastApplicationService(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(
        userId: UserId,
    ) = userRepository.findAvailableUserById(userId)
        .let(::checkNotNull)
        .let(::Cast)
}

package app.vercel.shiftup.features.user.account.application

import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.account.infra.UserRepository
import org.koin.core.annotation.Single

@Single
class GetUserUseCase(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(
        userId: UserId,
    ) = userRepository.findById(userId)
}

package app.vercel.shiftup.features.user.account.application

import app.vercel.shiftup.features.user.account.domain.model.AvailableUser
import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.account.infra.AvailableUserRepository
import org.koin.core.annotation.Single

@Single
class GetAvailableUserUseCase(
    private val availableUserRepository: AvailableUserRepository,
) {
    suspend operator fun invoke(userId: UserId): AvailableUser? {
        return availableUserRepository.findById(userId)
    }
}

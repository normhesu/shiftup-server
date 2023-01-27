package app.vercel.shiftup.features.user.account.application

import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.account.domain.model.value.Name
import app.vercel.shiftup.features.user.account.infra.AvailableUserRepository
import org.koin.core.annotation.Single

@Single
class GetUserNameUseCase(
    private val availableUserRepository: AvailableUserRepository,
) {
    suspend operator fun invoke(userId: UserId): Name? {
        return availableUserRepository.findById(userId)?.name
    }
}

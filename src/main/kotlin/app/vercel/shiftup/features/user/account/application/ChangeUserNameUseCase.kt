package app.vercel.shiftup.features.user.account.application

import app.vercel.shiftup.features.user.account.domain.model.User
import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.account.domain.model.value.Name
import app.vercel.shiftup.features.user.account.infra.AvailableUserRepository
import app.vercel.shiftup.features.user.account.infra.UserRepository
import org.koin.core.annotation.Single

@Single
class ChangeUserNameUseCase(
    private val userRepository: UserRepository,
    private val availableUserRepository: AvailableUserRepository,
) {
    suspend operator fun invoke(userId: UserId, name: Name) {
        val availableUser = availableUserRepository.findById(userId).let(::checkNotNull)
        userRepository.replace(User(availableUser).changeName(name))
    }
}

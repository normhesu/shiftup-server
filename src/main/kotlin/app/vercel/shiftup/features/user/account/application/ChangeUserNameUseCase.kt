package app.vercel.shiftup.features.user.account.application

import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.account.domain.model.value.Name
import app.vercel.shiftup.features.user.account.infra.UserRepository
import org.koin.core.annotation.Single

@Single
class ChangeUserNameUseCase(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(userId: UserId, name: Name) {
        val user = userRepository.findById(userId).let(::checkNotNull)
        userRepository.replace(user.changeName(name))
    }
}

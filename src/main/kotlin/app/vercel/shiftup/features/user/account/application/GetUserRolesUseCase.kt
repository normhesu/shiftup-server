package app.vercel.shiftup.features.user.account.application

import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.account.infra.UserRepository
import app.vercel.shiftup.features.user.domain.model.value.Role
import org.koin.core.annotation.Single

@Single
class GetUserRolesUseCase(
    private val userRepository: UserRepository,
) {
    // ユーザーが存在しない場合はnull、無効な場合は空のSetを返します
    suspend operator fun invoke(userId: UserId): Set<Role>? {
        if (userRepository.contains(userId).not()) return null
        return userRepository.findAvailableUserById(userId)?.roles.orEmpty()
    }
}

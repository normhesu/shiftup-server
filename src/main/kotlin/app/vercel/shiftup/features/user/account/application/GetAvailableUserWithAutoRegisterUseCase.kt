package app.vercel.shiftup.features.user.account.application

import app.vercel.shiftup.features.user.account.domain.model.AvailableUser
import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.account.domain.model.value.Name
import app.vercel.shiftup.features.user.account.infra.UserRepository
import app.vercel.shiftup.features.user.domain.model.value.Email
import app.vercel.shiftup.features.user.domain.model.value.SchoolProfile
import app.vercel.shiftup.features.user.invite.domain.service.GetInviteDomainService
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import org.koin.core.annotation.Single

@Single
class GetAvailableUserWithAutoRegisterUseCase(
    private val getInviteDomainService: GetInviteDomainService,
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(
        userId: UserId,
        name: Name,
        emailFactory: () -> Email,
    ): Result<AvailableUser, LoginOrRegisterException> = runSuspendCatching {
        val email = runCatching(emailFactory).getOrElse {
            throw LoginOrRegisterException.InvalidUser()
        }
        val invite = getInviteDomainService(email) ?: throw LoginOrRegisterException.InvalidUser()

        userRepository.findAvailableUserById(userId) ?: AvailableUser(
            id = userId,
            name = name,
            schoolProfile = SchoolProfile(
                email = email,
                department = invite.department,
            ),
            position = invite.position,
        ).also {
            userRepository.add(it)
        }
    }.mapError {
        when (it) {
            is LoginOrRegisterException -> it
            else -> LoginOrRegisterException.Other(it)
        }
    }
}

sealed class LoginOrRegisterException : Exception() {
    class InvalidUser : LoginOrRegisterException()
    class Other(e: Throwable) : LoginOrRegisterException() {
        override val message = e.message
    }
}

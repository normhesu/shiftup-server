package app.vercel.shiftup.features.user.account.application

import app.vercel.shiftup.features.user.account.domain.model.User
import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.account.domain.model.value.Name
import app.vercel.shiftup.features.user.account.infra.UserRepository
import app.vercel.shiftup.features.user.domain.model.value.NeecEmail
import app.vercel.shiftup.features.user.invited.domain.model.value.FirstManager
import app.vercel.shiftup.features.user.invited.domain.service.GetInvitedUserDomainService
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import org.koin.core.annotation.Single

@Single
class GetUserWithAutoRegisterUseCase(
    private val getInvitedUserDomainService: GetInvitedUserDomainService,
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(
        userId: UserId,
        name: Name,
        emailFactory: () -> NeecEmail,
        firstManager: FirstManager,
    ): Result<User, LoginOrRegisterException> = runSuspendCatching {
        userRepository.findById(userId)?.let { return@runSuspendCatching it }

        val invitedUser = getInvitedUserDomainService(
            firstManager = firstManager,
            email = runCatching(emailFactory).getOrElse {
                throw LoginOrRegisterException.InvalidUser()
            },
        ) ?: throw LoginOrRegisterException.InvalidUser()

        User(
            id = userId,
            name = name,
            department = invitedUser.department,
            studentNumber = invitedUser.studentNumber,
            roles = invitedUser.roles,
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

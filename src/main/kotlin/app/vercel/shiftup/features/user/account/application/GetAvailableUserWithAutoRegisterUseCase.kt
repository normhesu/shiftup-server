package app.vercel.shiftup.features.user.account.application

import app.vercel.shiftup.features.user.account.domain.model.AvailableUser
import app.vercel.shiftup.features.user.account.domain.model.User
import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.account.domain.model.value.Name
import app.vercel.shiftup.features.user.account.infra.UserRepository
import app.vercel.shiftup.features.user.domain.model.value.Email
import app.vercel.shiftup.features.user.domain.model.value.SchoolProfile
import app.vercel.shiftup.features.user.invite.domain.model.Invite
import app.vercel.shiftup.features.user.invite.domain.model.value.FirstManager
import app.vercel.shiftup.features.user.invite.infra.InviteRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.koin.core.annotation.Single

@Single
class GetAvailableUserWithAutoRegisterUseCase(
    private val userRepository: UserRepository,
    private val inviteRepository: InviteRepository,
    private val firstManager: FirstManager,
) {
    suspend operator fun invoke(
        userId: UserId,
        name: Name,
        emailFactory: () -> Email,
    ): Result<AvailableUser, LoginOrRegisterException> = runSuspendCatching {
        getAvailableUserWithAutoRegister(userId, name, emailFactory)
    }.mapError {
        when (it) {
            is LoginOrRegisterException -> it
            else -> LoginOrRegisterException.Other(it)
        }
    }

    private suspend fun getAvailableUserWithAutoRegister(
        userId: UserId,
        name: Name,
        emailFactory: () -> Email,
    ): AvailableUser = coroutineScope {
        val email = runCatching(emailFactory).getOrElse {
            throw LoginOrRegisterException.InvalidUser()
        }
        val inviteDeferred = async {
            getInvite(
                inviteEmail = email,
                firstManagerEmail = firstManager.email,
            )
        }
        val userDeferred = async { userRepository.findAvailableUserById(userId) }

        // ユーザー登録済みでも招待が取り消された場合はログイン出来ないようにするため、
        // ここでinviteDeferredをawaitして招待済みかチェックする
        val invite = inviteDeferred.await() ?: throw LoginOrRegisterException.InvalidUser()

        userDeferred.await() ?: AvailableUser(
            id = userId,
            name = name,
            schoolProfile = SchoolProfile(
                email = email,
                department = invite.department,
            ),
            position = invite.position,
        ).also {
            userRepository.addOrNothing(User(it))
        }
    }

    private suspend fun getInvite(
        inviteEmail: Email,
        firstManagerEmail: Email,
    ) = inviteRepository.findByEmail(inviteEmail)
        ?: if (inviteEmail == firstManagerEmail) {
            Invite(firstManager)
        } else {
            null
        }
}

sealed class LoginOrRegisterException : Exception() {
    class InvalidUser : LoginOrRegisterException()
    class Other(override val cause: Throwable) : LoginOrRegisterException()
}

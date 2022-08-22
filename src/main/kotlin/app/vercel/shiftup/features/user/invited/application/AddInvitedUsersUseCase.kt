package app.vercel.shiftup.features.user.invited.application

import app.vercel.shiftup.features.user.invited.domain.model.InvitedUser
import app.vercel.shiftup.features.user.invited.infra.InvitedUserRepository
import org.koin.core.annotation.Single

@Single
class AddInvitedUsersUseCase(
    private val invitedUserRepository: InvitedUserRepository,
) {
    suspend operator fun invoke(invitedUser: InvitedUser) {
        val invited = invitedUserRepository.findByStudentNumber(
            invitedUser.studentNumber,
        ) != null
        require(!invited) { "既に招待されています" }
        invitedUserRepository.add(invitedUser)
    }
}

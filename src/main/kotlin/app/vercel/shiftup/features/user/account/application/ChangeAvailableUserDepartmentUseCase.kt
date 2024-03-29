package app.vercel.shiftup.features.user.account.application

import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.account.infra.AvailableUserRepository
import app.vercel.shiftup.features.user.domain.model.value.Department
import app.vercel.shiftup.features.user.invite.domain.model.Invite
import app.vercel.shiftup.features.user.invite.domain.model.value.FirstManager
import app.vercel.shiftup.features.user.invite.infra.InviteRepository
import org.koin.core.annotation.Single

@Single
class ChangeAvailableUserDepartmentUseCase(
    private val availableUserRepository: AvailableUserRepository,
    private val inviteRepository: InviteRepository,
    private val firstManager: FirstManager,
) {
    suspend operator fun invoke(
        userId: UserId,
        department: Department
    ) {
        val user = availableUserRepository.findById(userId).let(::checkNotNull)
        val invite = inviteRepository.findByStudentNumber(user.studentNumber) ?: run {
            check(user.studentNumber == firstManager.studentNumber)
            Invite(firstManager)
        }
        return inviteRepository.addOrReplace(invite.changeDepartment(department))
    }
}

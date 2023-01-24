package app.vercel.shiftup.features.user.invite.application

import app.vercel.shiftup.features.user.account.domain.model.AvailableUser
import app.vercel.shiftup.features.user.account.infra.UserRepository
import app.vercel.shiftup.features.user.domain.model.value.StudentNumber
import app.vercel.shiftup.features.user.invite.domain.model.Invite
import app.vercel.shiftup.features.user.invite.infra.InviteRepository
import org.koin.core.annotation.Single

/**
 * ユーザーが一度もログインをしていない場合、氏名はnullになります
 */
@Single
class GetAllInvitesWithAvailableUserOrNullUseCase(
    private val inviteRepository: InviteRepository,
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(): List<Pair<Invite, AvailableUser?>> {
        val invites = inviteRepository.findAll()
        val availableUsers: Map<StudentNumber, AvailableUser> = userRepository.findAvailableUserByStudentNumbers(
            invites.map { it.studentNumber }
        ).associateBy {
            it.studentNumber
        }

        return invites.associateWith {
            availableUsers[it.studentNumber]
        }.map {
            it.key to it.value
        }
    }
}

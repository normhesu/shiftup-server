package app.vercel.shiftup.features.user.invite.application

import app.vercel.shiftup.features.user.account.domain.model.value.Name
import app.vercel.shiftup.features.user.account.infra.UserRepository
import app.vercel.shiftup.features.user.domain.model.value.StudentNumber
import app.vercel.shiftup.features.user.invite.domain.model.Invite
import app.vercel.shiftup.features.user.invite.infra.InviteRepository
import org.koin.core.annotation.Single

/**
 * ユーザーが一度もログインをしていない場合、氏名はnullになります
 */
@Single
class GetAllInvitesWithNameOrNullUseCase(
    private val inviteRepository: InviteRepository,
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(): Map<Invite, Name?> {
        val invites = inviteRepository.findAll()
        val names: Map<StudentNumber, Name> = userRepository.findAvailableUserByStudentNumbers(
            invites.map { it.studentNumber }
        ).associate {
            it.studentNumber to it.name
        }

        return invites.associateWith {
            names[it.studentNumber]
        }
    }
}

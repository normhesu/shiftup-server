package app.vercel.shiftup.features.user.invite.application

import app.vercel.shiftup.features.user.domain.model.value.StudentNumber
import app.vercel.shiftup.features.user.invite.domain.model.Invite
import app.vercel.shiftup.features.user.invite.infra.InviteRepository
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class AddInviteUseCaseTest : FreeSpec({
    val repository: InviteRepository = mockk(relaxUnitFun = true)
    val useCase = AddInviteUseCase(
        inviteRepository = repository
    )

    afterEach {
        clearMocks(repository)
    }

    "AddInviteUseCase" - {
        val studentNumber = StudentNumber("G000C0000")
        val invite = Invite(
            studentNumber = studentNumber,
            mockk(relaxed = true), mockk(relaxed = true),
        )

        "ユーザーがまだ招待されていない場合は追加する" {
            coEvery {
                repository.findByStudentNumber(studentNumber)
            } returns null

            useCase(invite)
            coVerify(exactly = 1) {
                repository.add(invite)
            }
        }

        "ユーザーが既に招待されている場合は、追加せずにIllegalArgumentExceptionを投げる" {
            coEvery {
                repository.findByStudentNumber(studentNumber)
            } returns invite

            shouldThrowExactly<IllegalArgumentException> {
                useCase(invite)
            }
            coVerify(exactly = 0) {
                repository.add(invite)
            }
        }
    }
})

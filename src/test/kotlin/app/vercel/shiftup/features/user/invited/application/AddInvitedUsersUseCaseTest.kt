package app.vercel.shiftup.features.user.invited.application

import app.vercel.shiftup.features.user.domain.model.value.StudentNumber
import app.vercel.shiftup.features.user.invited.domain.model.InvitedUser
import app.vercel.shiftup.features.user.invited.infra.InvitedUserRepository
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class AddInvitedUsersUseCaseTest : FreeSpec({
    val repository: InvitedUserRepository = mockk(relaxUnitFun = true)
    val useCase = AddInvitedUsersUseCase(
        invitedUserRepository = repository
    )

    afterEach {
        clearMocks(repository)
    }

    "AddInvitedUsersUseCase" - {
        val studentNumber = StudentNumber("G000C0000")
        val invitedUser = InvitedUser(
            studentNumber = studentNumber,
            mockk(relaxed = true), mockk(relaxed = true),
        )

        "ユーザーがまだ招待されていない場合は追加する" {
            coEvery {
                repository.findByStudentNumber(studentNumber)
            } returns null

            useCase(invitedUser)
            coVerify(exactly = 1) {
                repository.add(invitedUser)
            }
        }

        "ユーザーが既に招待されている場合は、追加せずにIllegalArgumentExceptionを投げる" {
            coEvery {
                repository.findByStudentNumber(studentNumber)
            } returns invitedUser

            shouldThrowExactly<IllegalArgumentException> {
                useCase(invitedUser)
            }
            coVerify(exactly = 0) {
                repository.add(invitedUser)
            }
        }
    }
})

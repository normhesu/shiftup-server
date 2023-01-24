package app.vercel.shiftup.features.user.invite.application

import app.vercel.shiftup.features.user.domain.model.value.Email
import app.vercel.shiftup.features.user.domain.model.value.NeecDepartment
import app.vercel.shiftup.features.user.domain.model.value.SchoolProfile
import app.vercel.shiftup.features.user.domain.model.value.StudentNumber
import app.vercel.shiftup.features.user.invite.domain.model.Invite
import app.vercel.shiftup.features.user.invite.domain.model.value.FirstManager
import app.vercel.shiftup.features.user.invite.infra.InviteRepository
import com.github.michaelbull.result.Err
import com.mongodb.MongoException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class AddInviteUseCaseTest : FreeSpec({
    "AddInviteUseCase" - {
        val repository: InviteRepository = mockk(relaxUnitFun = true)
        val studentNumber = StudentNumber("G000C0000")
        val invite = Invite(
            studentNumber = studentNumber,
            mockk(relaxed = true), mockk(relaxed = true),
        )

        "正常系" - {
            "ユーザーがまだ招待されていない場合は追加する" {
                val useCase = AddInviteUseCase(
                    inviteRepository = repository,
                    firstManager = FirstManager(
                        schoolProfile = SchoolProfile(
                            department = NeecDepartment.C2,
                            email = Email("g000c9999@g.neec.ac.jp").also {
                                it.studentNumber shouldNotBe studentNumber
                            }
                        )
                    ),
                )
                coEvery {
                    repository.findByStudentNumber(studentNumber)
                } returns null

                useCase(invite)
                coVerify(exactly = 1) {
                    repository.add(invite)
                }
            }
        }

        "異常系" - {
            "ユーザーが既に招待されている場合は、追加せずにErr<AddInviteUseCaseException.Invited>を返す" {
                val useCase = AddInviteUseCase(
                    inviteRepository = repository,
                    firstManager = FirstManager(
                        schoolProfile = SchoolProfile(
                            department = NeecDepartment.C2,
                            email = Email("g000c9999@g.neec.ac.jp").also {
                                it.studentNumber shouldNotBe studentNumber
                            }
                        )
                    ),
                )

                val duplicateKeyCode = 11000
                val mockMongoException: MongoException = mockk(relaxed = true)

                coEvery {
                    mockMongoException.code
                } answers {
                    duplicateKeyCode
                }

                coEvery {
                    repository.add(invite)
                } throws mockMongoException

                useCase(invite).shouldBeInstanceOf<Err<AddInviteUseCaseException.Invited>>()
            }

            "ユーザーがfirstManagerとして設定されている場合、positionがManager出なければIllegalArgumentExceptionをthrowする" {
                val useCase = AddInviteUseCase(
                    inviteRepository = repository,
                    firstManager = FirstManager(
                        schoolProfile = SchoolProfile(
                            department = NeecDepartment.C2,
                            email = Email("g000c0000@g.neec.ac.jp").also {
                                it.studentNumber shouldBe studentNumber
                            }
                        )
                    ),
                )
                coEvery {
                    repository.findByStudentNumber(studentNumber)
                } returns null

                shouldThrow<IllegalArgumentException> {
                    useCase(invite)
                }
            }
        }
    }
})

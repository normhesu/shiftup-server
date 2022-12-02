package app.vercel.shiftup.features.user.account.application

import app.vercel.shiftup.features.user.account.domain.model.User
import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.account.infra.UserRepository
import app.vercel.shiftup.features.user.domain.model.value.Email
import app.vercel.shiftup.features.user.domain.model.value.NeecDepartment
import app.vercel.shiftup.features.user.domain.model.value.SchoolProfile
import app.vercel.shiftup.features.user.domain.model.value.StudentNumber
import app.vercel.shiftup.features.user.invite.domain.model.Invite
import app.vercel.shiftup.features.user.invite.domain.model.value.FirstManager
import app.vercel.shiftup.features.user.invite.domain.model.value.Position
import app.vercel.shiftup.features.user.invite.domain.service.GetInviteDomainService
import app.vercel.shiftup.features.user.invite.infra.InviteRepository
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.utils.io.errors.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class GetUserWithAutoRegisterUseCaseTest : FreeSpec({
    val userRepository = mockk<UserRepository>(relaxUnitFun = true)
    val inviteRepository: InviteRepository = mockk()
    val useCase = GetUserWithAutoRegisterUseCase(
        userRepository = userRepository,
        getInviteDomainService = GetInviteDomainService(
            inviteRepository = inviteRepository,
        )
    )

    "GetUserWithAutoRegisterUseCase" - {
        val userId: UserId = mockk(relaxed = true)

        "アカウント登録済みの場合、ユーザーを返す" {
            coEvery {
                inviteRepository.findByEmail(Email("g020c0000@g.neec.ac.jp"))
            } returns Invite(
                department = NeecDepartment.C2,
                position = Position.Cast,
                studentNumber = StudentNumber("G020C0000"),
            )
            val resultUser = User(
                id = userId,
                name = mockk(relaxed = true),
                schoolProfile = SchoolProfile(
                    email = Email("g020c0000@g.neec.ac.jp"),
                    department = NeecDepartment.C2,
                ),
                position = Position.Cast,
            )
            coEvery {
                userRepository.findById(userId)
            } returns resultUser

            useCase(
                userId = userId,
                emailFactory = { Email("g020c0000@g.neec.ac.jp") },
                name = mockk(relaxed = true),
                firstManager = mockk(relaxed = true),
            ) shouldBe Ok(resultUser)
        }

        "アカウント未登録の場合" - {
            coEvery {
                userRepository.findById(userId)
            } returns null

            val position = Position.Manager
            val resultUser = User(
                id = userId,
                name = mockk(relaxed = true),
                schoolProfile = SchoolProfile(
                    email = Email("g000c0000@g.neec.ac.jp"),
                    department = mockk(relaxed = true),
                ),
                position = position,
            )

            val notAllowedFirstManager = FirstManager(
                SchoolProfile(
                    email = Email("g999c9999@g.neec.ac.jp").also {
                        it shouldNotBe resultUser.email
                    },
                    department = mockk(relaxed = true),
                )
            )

            "招待されている場合、ユーザーを登録して返す" - {
                coEvery {
                    inviteRepository.findByEmail(resultUser.email)
                } returns Invite(
                    studentNumber = resultUser.studentNumber,
                    department = resultUser.department,
                    position = position,
                )

                useCase(
                    userId = userId,
                    name = resultUser.name,
                    emailFactory = { resultUser.email },
                    firstManager = notAllowedFirstManager,
                ) shouldBe Ok(resultUser)

                coVerify {
                    userRepository.add(resultUser)
                }
            }

            "招待されていない場合" - {
                coEvery {
                    inviteRepository.findByEmail(resultUser.email)
                } returns null

                "最初のアカウントとして登録可能な場合、ユーザーを登録して返す" {
                    useCase(
                        userId = userId,
                        name = resultUser.name,
                        emailFactory = { resultUser.email },
                        firstManager = FirstManager(
                            SchoolProfile(
                                email = resultUser.email,
                                department = resultUser.department,
                            )
                        )
                    ) shouldBe Ok(resultUser)

                    coVerify {
                        userRepository.add(resultUser)
                    }
                }

                "最初のアカウントとして登録不可な場合、Err(LoginOrRegisterException.InvalidUser())を返す" {
                    useCase(
                        userId = userId,
                        name = resultUser.name,
                        emailFactory = { resultUser.email },
                        firstManager = notAllowedFirstManager,
                    ).shouldBeInstanceOf<Err<LoginOrRegisterException.InvalidUser>>()
                }
            }
        }

        "その他の理由で失敗した場合、Err(LoginOrRegisterException.Other())を返す" {
            coEvery {
                userRepository.findById(UserId(any()))
            } throws IOException()

            useCase(
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true),
            ).shouldBeInstanceOf<Err<LoginOrRegisterException.Other>>()
        }
    }
})

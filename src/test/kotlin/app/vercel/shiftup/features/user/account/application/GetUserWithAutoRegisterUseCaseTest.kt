package app.vercel.shiftup.features.user.account.application

import app.vercel.shiftup.features.user.account.domain.model.AvailableUser
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
    val mockUserRepository = mockk<UserRepository>(relaxUnitFun = true)
    val mockInviteRepository: InviteRepository = mockk()

    val mockUserId: UserId = mockk(relaxed = true)

    "GetUserWithAutoRegisterUseCase" - {
        "アカウント登録済みの場合、ユーザーを返す" {
            val useCase = GetAvailableUserWithAutoRegisterUseCase(
                userRepository = mockUserRepository,
                inviteRepository = mockInviteRepository,
                firstManager = mockk(relaxed = true),
            )

            coEvery {
                mockInviteRepository.findByEmail(Email("g020c0000@g.neec.ac.jp"))
            } returns Invite(
                department = NeecDepartment.C2,
                position = Position.Cast,
                studentNumber = StudentNumber("G020C0000"),
            )

            val resultUser = AvailableUser(
                id = mockUserId,
                name = mockk(relaxed = true),
                schoolProfile = SchoolProfile(
                    email = Email("g020c0000@g.neec.ac.jp"),
                    department = NeecDepartment.C2,
                ),
                position = Position.Cast,
            )
            coEvery {
                mockUserRepository.findAvailableUserById(mockUserId)
            } returns resultUser

            useCase(
                userId = mockk(relaxed = true),
                emailFactory = { Email("g020c0000@g.neec.ac.jp") },
                name = mockk(relaxed = true),
            ) shouldBe Ok(resultUser)
        }

        "アカウント未登録の場合" - {
            coEvery {
                mockUserRepository.findAvailableUserById(mockUserId)
            } returns null

            val position = Position.Manager
            val resultAvailableUser = AvailableUser(
                id = mockUserId,
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
                        it shouldNotBe resultAvailableUser.email
                    },
                    department = mockk(relaxed = true),
                )
            )

            "招待されている場合、ユーザーを登録して返す" {
                val useCase = GetAvailableUserWithAutoRegisterUseCase(
                    userRepository = mockUserRepository,
                    inviteRepository = mockInviteRepository,
                    firstManager = notAllowedFirstManager,
                )

                coEvery {
                    mockInviteRepository.findByEmail(resultAvailableUser.email)
                } returns Invite(
                    studentNumber = resultAvailableUser.studentNumber,
                    department = resultAvailableUser.department,
                    position = position,
                )

                useCase(
                    userId = mockk(relaxed = true),
                    name = resultAvailableUser.name,
                    emailFactory = { resultAvailableUser.email },
                ) shouldBe Ok(resultAvailableUser)

                coVerify {
                    mockUserRepository.addOrNothing(User(resultAvailableUser))
                }
            }

            "招待されていない場合" - {
                coEvery {
                    mockInviteRepository.findByEmail(resultAvailableUser.email)
                } returns null

                "最初のアカウントとして登録可能な場合、ユーザーを登録して返す" {
                    val useCase = GetAvailableUserWithAutoRegisterUseCase(
                        userRepository = mockUserRepository,
                        inviteRepository = mockInviteRepository,
                        firstManager = FirstManager(
                            SchoolProfile(
                                email = resultAvailableUser.email,
                                department = resultAvailableUser.department,
                            ),
                        ),
                    )
                    useCase(
                        userId = mockk(relaxed = true),
                        name = resultAvailableUser.name,
                        emailFactory = { resultAvailableUser.email },
                    ) shouldBe Ok(resultAvailableUser)

                    coVerify {
                        mockUserRepository.addOrNothing(User(resultAvailableUser))
                    }
                }

                "最初のアカウントとして登録不可な場合、Err(LoginOrRegisterException.InvalidUser())を返す" {
                    val useCase = GetAvailableUserWithAutoRegisterUseCase(
                        userRepository = mockUserRepository,
                        inviteRepository = mockInviteRepository,
                        firstManager = notAllowedFirstManager,
                    )
                    useCase(
                        userId = mockk(relaxed = true),
                        name = resultAvailableUser.name,
                        emailFactory = { resultAvailableUser.email },
                    ).shouldBeInstanceOf<Err<LoginOrRegisterException.InvalidUser>>()
                }
            }
        }

        "その他の理由で失敗した場合、Err(LoginOrRegisterException.Other())を返す" {
            val useCase = GetAvailableUserWithAutoRegisterUseCase(
                userRepository = mockUserRepository,
                inviteRepository = mockInviteRepository,
                firstManager = mockk(relaxed = true),
            )

            coEvery {
                mockUserRepository.findAvailableUserById(UserId(any()))
            } throws IOException()

            useCase(
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true),
            ).shouldBeInstanceOf<Err<LoginOrRegisterException.Other>>()
        }
    }
})

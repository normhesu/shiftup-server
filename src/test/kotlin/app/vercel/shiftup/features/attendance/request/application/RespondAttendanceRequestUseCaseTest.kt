package app.vercel.shiftup.features.attendance.request.application

import app.vercel.shiftup.features.attendance.request.domain.model.AttendanceRequest
import app.vercel.shiftup.features.attendance.request.domain.model.value.AttendanceRequestState
import app.vercel.shiftup.features.attendance.request.infra.AttendanceRequestRepository
import app.vercel.shiftup.features.user.account.domain.model.AvailableUser
import app.vercel.shiftup.features.user.account.infra.UserRepository
import app.vercel.shiftup.features.user.invite.domain.model.value.Position
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getOrThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.mockk

class RespondAttendanceRequestUseCaseTest : FreeSpec({
    "RespondAttendanceRequestUseCase" - {
        val mockUserRepository: UserRepository = mockk()
        val mockAttendanceRequestRepository: AttendanceRequestRepository = mockk(relaxUnitFun = true)
        val useCase = RespondAttendanceRequestUseCase(
            userRepository = mockUserRepository,
            attendanceRequestRepository = mockAttendanceRequestRepository,
        )

        coEvery {
            mockUserRepository.findAvailableUserById(any())
        } returns AvailableUser(
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            position = Position.Cast,
        )

        "正常系" - {
            "stateがBlankの場合、Ok(Unit)を返す" {
                coEvery {
                    mockAttendanceRequestRepository.findById(any())
                } returns AttendanceRequest(
                    mockk(relaxed = true),
                    mockk(relaxed = true),
                ).also {
                    it.state shouldBe AttendanceRequestState.Blank
                }

                val result = useCase(
                    mockk(relaxed = true),
                    mockk(relaxed = true),
                    state = AttendanceRequestState.Accepted,
                )

                result shouldBe Ok(Unit)
            }
        }

        "異常系" - {
            "stateがBlankでない場合、Err<IllegalStateException>を返す" {
                coEvery {
                    mockAttendanceRequestRepository.findById(any())
                } returns AttendanceRequest(
                    mockk(relaxed = true),
                    mockk(relaxed = true),
                ).respond(AttendanceRequestState.Declined)
                    .getOrThrow()
                    .also {
                        it.state shouldNotBe AttendanceRequestState.Blank
                    }

                val result = useCase(
                    mockk(relaxed = true),
                    mockk(relaxed = true),
                    state = AttendanceRequestState.Accepted,
                )

                result.shouldBeInstanceOf<Err<IllegalStateException>>()
            }
        }
    }
})

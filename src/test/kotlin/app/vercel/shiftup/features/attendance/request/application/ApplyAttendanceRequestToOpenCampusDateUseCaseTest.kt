package app.vercel.shiftup.features.attendance.request.application

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.request.domain.model.AttendanceRequest
import app.vercel.shiftup.features.attendance.request.infra.AttendanceRequestRepository
import app.vercel.shiftup.features.user.account.application.service.GetCastsByCastIdsApplicationService
import app.vercel.shiftup.features.user.account.domain.model.AvailableUser
import app.vercel.shiftup.features.user.account.domain.model.Cast
import app.vercel.shiftup.features.user.account.domain.model.CastId
import app.vercel.shiftup.features.user.account.domain.model.UserId
import app.vercel.shiftup.features.user.invite.domain.model.value.Position
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DoNotParallelize
import io.kotest.core.spec.style.FreeSpec
import io.mockk.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@DoNotParallelize
class ApplyAttendanceRequestToOpenCampusDateUseCaseTest : FreeSpec({
    beforeSpec {
        mockkObject(Clock.System)
        every {
            Clock.System.now()
        } returns Instant.parse(
            "2022-01-01T00:00:00+09:00",
        )
    }

    afterSpec {
        unmockkObject(Clock.System)
    }

    "ApplyAttendanceRequestToOpenCampusDateUseCase" - {
        val mockAttendanceRequestRepository: AttendanceRequestRepository = mockk(relaxUnitFun = true)
        val mockGetCastsByCastIdsApplicationService: GetCastsByCastIdsApplicationService = mockk()
        val useCase = ApplyAttendanceRequestToOpenCampusDateUseCase(
            attendanceRequestRepository = mockAttendanceRequestRepository,
            getCastsByCastIdsApplicationService = mockGetCastsByCastIdsApplicationService,
        )
        "正常系" - {
            "出勤依頼が空の場合、受け取ったCastIdの出勤依頼を追加する" {
                val openCampusDate = OpenCampusDate(
                    LocalDate(2022, 2, 1),
                )
                val userIds = List(10) {
                    UserId(it.toString())
                }.toSet()
                val castIds = userIds.map(CastId::unsafe)

                val casts = userIds.map {
                    AvailableUser(
                        id = it,
                        name = mockk(relaxed = true),
                        position = Position.Cast,
                        schoolProfile = mockk(relaxed = true)
                    ).let(::Cast)
                }

                coEvery {
                    mockGetCastsByCastIdsApplicationService(castIds)
                } returns casts

                coEvery {
                    mockAttendanceRequestRepository.findByOpenCampusDate(openCampusDate)
                } returns emptyList()

                useCase(
                    openCampusDate = openCampusDate,
                    userIds = userIds,
                )

                coVerify {
                    mockAttendanceRequestRepository.addAndRemoveAll(
                        addAttendanceRequests = casts.map {
                            AttendanceRequest(
                                castId = it.id,
                                openCampusDate = openCampusDate,
                            )
                        }.toSet(),
                        removeAttendanceRequests = emptySet(),
                    )
                }
            }

            "受け取ったCastIdが全て登録済みの出勤依頼のCastIdの場合、なにもしない" {
                val openCampusDate = OpenCampusDate(
                    LocalDate(2022, 2, 1),
                )
                val userIds = List(10) {
                    UserId(it.toString())
                }.toSet()
                val castIds = userIds.map(CastId::unsafe)

                val casts = userIds.map {
                    AvailableUser(
                        id = it,
                        name = mockk(relaxed = true),
                        position = Position.Cast,
                        schoolProfile = mockk(relaxed = true)
                    ).let(::Cast)
                }

                coEvery {
                    mockGetCastsByCastIdsApplicationService(castIds)
                } returns casts

                coEvery {
                    mockAttendanceRequestRepository.findByOpenCampusDate(openCampusDate)
                } returns casts.map {
                    AttendanceRequest(
                        castId = it.id,
                        openCampusDate = openCampusDate,
                    )
                }

                useCase(
                    openCampusDate = openCampusDate,
                    userIds = userIds,
                )

                coVerify {
                    mockAttendanceRequestRepository.addAndRemoveAll(
                        addAttendanceRequests = emptySet(),
                        removeAttendanceRequests = emptySet(),
                    )
                }
            }

            "受け取ったCastIdの中にすでに登録済みの出勤依頼のCastIdがある場合、差分のみを追加する" {
                val openCampusDate = OpenCampusDate(
                    LocalDate(2022, 2, 1),
                )
                val userIds = List(10) {
                    UserId(it.toString())
                }.toSet()
                val castIds = userIds.map(CastId::unsafe)

                val users = userIds.map {
                    AvailableUser(
                        id = it,
                        name = mockk(relaxed = true),
                        position = Position.Cast,
                        schoolProfile = mockk(relaxed = true)
                    )
                }

                val casts = users.map(::Cast)

                val savedRequests = users.take(3).map {
                    AttendanceRequest(
                        castId = Cast(it).id,
                        openCampusDate = openCampusDate,
                    )
                }

                coEvery {
                    mockGetCastsByCastIdsApplicationService(castIds)
                } returns casts

                coEvery {
                    mockAttendanceRequestRepository.findByOpenCampusDate(openCampusDate)
                } returns savedRequests

                useCase(
                    openCampusDate = openCampusDate,
                    userIds = userIds,
                )

                coVerify {
                    mockAttendanceRequestRepository.addAndRemoveAll(
                        addAttendanceRequests = users.drop(savedRequests.size).map {
                            AttendanceRequest(
                                castId = Cast(it).id,
                                openCampusDate = openCampusDate,
                            )
                        }.toSet(),
                        removeAttendanceRequests = emptySet(),
                    )
                }
            }

            "受け取ったCastIdが空で、登録済みの出勤依頼ある場合は出勤依頼を全て削除する" {
                val openCampusDate = OpenCampusDate(
                    LocalDate(2022, 2, 1),
                )

                val userIds = emptySet<UserId>()
                val castIds = userIds.map(CastId::unsafe)

                coEvery {
                    mockGetCastsByCastIdsApplicationService(castIds)
                } returns emptyList()

                val savedRequests = List(10) {
                    AttendanceRequest(
                        castId = CastId.unsafe(UserId(it.toString())),
                        openCampusDate = openCampusDate,
                    )
                }

                coEvery {
                    mockAttendanceRequestRepository.findByOpenCampusDate(openCampusDate)
                } returns savedRequests

                useCase(
                    openCampusDate = openCampusDate,
                    userIds = userIds,
                )

                coVerify {
                    mockAttendanceRequestRepository.addAndRemoveAll(
                        addAttendanceRequests = emptySet(),
                        removeAttendanceRequests = savedRequests.toSet(),
                    )
                }
            }

            "登録済みの出勤依頼のCastId内に受け取ったCastIdがある場合、差分のみを削除する" {
                val openCampusDate = OpenCampusDate(
                    LocalDate(2022, 2, 1),
                )

                val savedRequests = List(10) {
                    AttendanceRequest(
                        castId = CastId.unsafe(UserId(it.toString())),
                        openCampusDate = openCampusDate,
                    )
                }

                val userIds = savedRequests.take(3).map { it.castId.value }.toSet()
                val castIds = userIds.map(CastId::unsafe)

                val casts = userIds.map {
                    AvailableUser(
                        id = it,
                        name = mockk(relaxed = true),
                        position = Position.Cast,
                        schoolProfile = mockk(relaxed = true)
                    ).let(::Cast)
                }

                coEvery {
                    mockGetCastsByCastIdsApplicationService(castIds)
                } returns casts

                coEvery {
                    mockAttendanceRequestRepository.findByOpenCampusDate(openCampusDate)
                } returns savedRequests

                useCase(
                    openCampusDate = openCampusDate,
                    userIds = userIds,
                )

                coVerify {
                    mockAttendanceRequestRepository.addAndRemoveAll(
                        addAttendanceRequests = emptySet(),
                        removeAttendanceRequests = savedRequests.drop(userIds.size).toSet(),
                    )
                }
            }
            "受け取ったCastIdの中にすでに登録済みの出勤依頼のCastIdがあり、" +
                "登録済みの出勤依頼のCastId内に受け取ったCastIdがある場合、" +
                "差分を適用する" {
                    val openCampusDate = OpenCampusDate(
                        LocalDate(2022, 2, 1),
                    )

                    val userIds = setOf(1, 2, 4, 6, 7).map {
                        (UserId(it.toString()))
                    }.toSet()
                    val castIds = userIds.map(CastId::unsafe)

                    coEvery {
                        mockGetCastsByCastIdsApplicationService(castIds)
                    } returns userIds.map {
                        AvailableUser(
                            id = it,
                            name = mockk(relaxed = true),
                            position = Position.Cast,
                            schoolProfile = mockk(relaxed = true)
                        ).let(::Cast)
                    }

                    coEvery {
                        mockAttendanceRequestRepository.findByOpenCampusDate(openCampusDate)
                    } returns (3..5).map {
                        AttendanceRequest(
                            castId = CastId.unsafe(UserId(it.toString())),
                            openCampusDate = openCampusDate,
                        )
                    }

                    useCase(
                        openCampusDate = openCampusDate,
                        userIds = userIds,
                    )

                    coVerify {
                        mockAttendanceRequestRepository.addAndRemoveAll(
                            addAttendanceRequests = setOf(1, 2, 6, 7).map {
                                AttendanceRequest(
                                    castId = CastId.unsafe(UserId(it.toString())),
                                    openCampusDate = openCampusDate,
                                )
                            }.toSet(),
                            removeAttendanceRequests = setOf(3, 5).map {
                                AttendanceRequest(
                                    castId = CastId.unsafe(UserId(it.toString())),
                                    openCampusDate = openCampusDate,
                                )
                            }.toSet(),
                        )
                    }
                }
        }
        "異常系" - {
            "変更する日付が過去の場合、IllegalArgumentExceptionを投げる" {
                shouldThrow<IllegalArgumentException> {
                    useCase(
                        openCampusDate = OpenCampusDate(LocalDate(2021, 12, 31)),
                        userIds = emptySet(),
                    )
                }
            }
        }
    }
})

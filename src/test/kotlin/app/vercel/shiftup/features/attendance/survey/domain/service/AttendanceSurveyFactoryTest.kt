package app.vercel.shiftup.features.attendance.survey.domain.service

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurvey
import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurveyId
import app.vercel.shiftup.features.attendance.survey.domain.model.value.OpenCampusDates
import app.vercel.shiftup.features.core.domain.model.toTokyoLocalDateTime
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.annotation.DoNotParallelize
import io.kotest.core.spec.style.FreeSpec
import io.mockk.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@DoNotParallelize
class AttendanceSurveyFactoryTest : FreeSpec({
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

    "AttendanceSurveyFactory" - {
        val repository: AttendanceSurveyRepositoryInterface = mockk()
        val factory = AttendanceSurveyFactory(repository)
        "正常系" - {
            "名前が空白文字や空ではなく、日程が全て同じ年度で呼び出し日以降かつ、他のアンケートに含まれていない場合、生成できる" {
                coEvery {
                    repository.findAll()
                } returns listOf(
                    AttendanceSurvey.fromFactory(
                        name = "1",
                        openCampusSchedule = OpenCampusDates(
                            setOf(
                                OpenCampusDate(LocalDate(2022, 1, 3)),
                                OpenCampusDate(LocalDate(2022, 2, 1)),
                            )
                        ),
                        creationDate = Clock.System.now().toTokyoLocalDateTime().date,
                        available = true,
                        id = AttendanceSurveyId(),
                    )
                )
                shouldNotThrowAny {
                    factory(
                        name = "2",
                        openCampusSchedule = OpenCampusDates(
                            setOf(
                                OpenCampusDate(LocalDate(2022, 1, 1)),
                                OpenCampusDate(LocalDate(2022, 1, 2)),
                            )
                        ),
                    )
                }
            }
        }
        "異常系" - {
            "オープンキャンパスの日程が空の場合、IllegalArgumentExceptionを投げる" {
                coEvery { repository.findAll() } returns emptyList()
                shouldThrowExactly<IllegalArgumentException> {
                    factory(
                        name = "テスト",
                        openCampusSchedule = OpenCampusDates.empty(),
                    )
                }
            }
            "オープンキャンパスの日程が現在より古い場合、IllegalArgumentExceptionを投げる" {
                coEvery { repository.findAll() } returns emptyList()
                shouldThrowExactly<IllegalArgumentException> {
                    factory(
                        name = "テスト",
                        openCampusSchedule = OpenCampusDates(
                            setOf(OpenCampusDate(LocalDate(2021, 12, 31)))
                        ),
                    )
                }
            }
            "名前が空の場合、IllegalArgumentExceptionを投げる" {
                coEvery { repository.findAll() } returns emptyList()
                shouldThrowExactly<IllegalArgumentException> {
                    factory(
                        name = "",
                        openCampusSchedule = mockk(relaxed = true),
                    )
                }
            }
            "名前が空白文字のみの場合、IllegalArgumentExceptionを投げる" {
                coEvery { repository.findAll() } returns emptyList()
                shouldThrowExactly<IllegalArgumentException> {
                    factory(
                        name = " ",
                        openCampusSchedule = mockk(relaxed = true),
                    )
                }
            }
            "日程が他のアンケートに含まれている場合、IllegalArgumentExceptionを投げる" {
                coEvery {
                    repository.findAll()
                } returns listOf(
                    AttendanceSurvey.fromFactory(
                        name = "1",
                        openCampusSchedule = OpenCampusDates(
                            setOf(
                                OpenCampusDate(LocalDate(2022, 1, 1)),
                                OpenCampusDate(LocalDate(2022, 1, 2)),
                            )
                        ),
                        creationDate = Clock.System.now().toTokyoLocalDateTime().date,
                        available = true,
                        id = AttendanceSurveyId(),
                    )
                )
                shouldThrowExactly<IllegalArgumentException> {
                    factory(
                        name = "2",
                        openCampusSchedule = OpenCampusDates(
                            setOf(
                                OpenCampusDate(LocalDate(2022, 1, 1)),
                                OpenCampusDate(LocalDate(2022, 1, 3)),
                            )
                        ),
                    )
                }
            }
        }
    }
})

package app.vercel.shiftup.features.attendance.survey.answer.domain.service

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurvey
import app.vercel.shiftup.features.attendance.survey.domain.model.AttendanceSurveyId
import app.vercel.shiftup.features.attendance.survey.domain.model.value.SameFiscalYearOpenCampusDates
import app.vercel.shiftup.features.attendance.survey.domain.service.AttendanceSurveyRepositoryInterface
import app.vercel.shiftup.features.core.domain.model.toTokyoLocalDateTime
import app.vercel.shiftup.features.user.account.domain.model.Cast
import com.github.michaelbull.result.Err
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.DoNotParallelize
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@DoNotParallelize
class AttendanceSurveyAnswerFactoryTest : FreeSpec({
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

    "AttendanceSurveyAnswerFactory" - {
        val mockSurveyRepository: AttendanceSurveyRepositoryInterface = mockk()
        val factory = AttendanceSurveyAnswerFactory(mockSurveyRepository)
        val fakeOpenCampus = AttendanceSurvey.fromFactory(
            name = "テスト",
            openCampusSchedule = SameFiscalYearOpenCampusDates(
                setOf(
                    OpenCampusDate(LocalDate(2022, 4, 1))
                ),
            ),
            creationDate = Clock.System.now().toTokyoLocalDateTime().date,
            available = true,
            id = AttendanceSurveyId(),
        )

        "生成" - {
            "正常系" {
                val mockCast: Cast = mockk(relaxed = true)
                every { mockCast.inSchool(any()) } returns true
                coEvery { mockSurveyRepository.findById(any()) } returns fakeOpenCampus

                shouldNotThrowAny {
                    factory(
                        attendanceSurveyId = mockk(relaxed = true),
                        cast = mockCast,
                        availableDays = mockk(relaxed = true),
                    )
                }
            }
            "異常系" - {
                "アンケートが見つからない場合、Err(AttendanceSurveyAnswerFactoryException.NotFoundSurveyを返す" {
                    coEvery { mockSurveyRepository.findById(any()) } returns null
                    val actual = factory(
                        attendanceSurveyId = mockk(relaxed = true),
                        cast = mockk(relaxed = true),
                        availableDays = mockk(relaxed = true),
                    )
                    actual.shouldBeInstanceOf<Err<AttendanceSurveyAnswerFactoryException.NotFoundSurvey>>()
                }
                "アンケートが回答受付を終了している場合、Err(AttendanceSurveyAnswerFactoryException.CanNotAnswer)を返す" {
                    coEvery {
                        mockSurveyRepository.findById(any())
                    } returns fakeOpenCampus.changeAvailable(false)

                    val actual = factory(
                        attendanceSurveyId = mockk(relaxed = true),
                        cast = mockk(relaxed = true),
                        availableDays = mockk(relaxed = true),
                    )
                    actual.shouldBeInstanceOf<Err<AttendanceSurveyAnswerFactoryException.CanNotAnswer>>()
                }
                "アンケート内のオープンキャンパス開催日に学生が在籍していない場合、Err(AttendanceSurveyAnswerFactoryException.CanNotAnswer)を返す" {
                    val mockCast: Cast = mockk(relaxed = true)
                    every { mockCast.inSchool(any()) } returns false
                    coEvery { mockSurveyRepository.findById(any()) } returns fakeOpenCampus

                    val result = factory(
                        attendanceSurveyId = mockk(relaxed = true),
                        cast = mockCast,
                        availableDays = SameFiscalYearOpenCampusDates(
                            setOf(
                                OpenCampusDate(
                                    LocalDate(
                                        year = 2022,
                                        monthNumber = 4,
                                        dayOfMonth = 2,
                                    ),
                                )
                            ),
                        ),
                    )
                    result.shouldBeInstanceOf<Err<AttendanceSurveyAnswerFactoryException.CanNotAnswer>>()
                }
                "アンケート内のオープンキャンパス開催日以外の日にちが出勤可能日にある場合、IllegalArgumentExceptionを投げる" {
                    val mockCast: Cast = mockk(relaxed = true)
                    every { mockCast.inSchool(any()) } returns true
                    coEvery { mockSurveyRepository.findById(any()) } returns fakeOpenCampus
                    val openCampusDate = OpenCampusDate(
                        LocalDate(
                            year = 2022,
                            monthNumber = 4,
                            dayOfMonth = 2,
                        ),
                    )

                    (openCampusDate !in fakeOpenCampus.openCampusSchedule).shouldBeTrue()
                    shouldThrow<IllegalArgumentException> {
                        factory(
                            attendanceSurveyId = mockk(relaxed = true),
                            cast = mockCast,
                            availableDays = SameFiscalYearOpenCampusDates(
                                setOf(openCampusDate),
                            ),
                        )
                    }
                }
            }
        }
    }
})

package app.vercel.shiftup.features.attendancesurvey.answer.domain.service

import app.vercel.shiftup.features.attendancesurvey.domain.model.AttendanceSurvey
import app.vercel.shiftup.features.attendancesurvey.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendancesurvey.domain.model.value.OpenCampusDates
import app.vercel.shiftup.features.attendancesurvey.domain.service.AttendanceSurveyAnswerFactory
import app.vercel.shiftup.features.attendancesurvey.domain.service.AttendanceSurveyAnswerFactoryException
import app.vercel.shiftup.features.attendancesurvey.domain.service.AttendanceSurveyRepositoryInterface
import app.vercel.shiftup.features.user.account.domain.model.Cast
import com.github.michaelbull.result.Err
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

class AttendanceSurveyAnswerFactoryTest : FreeSpec({
    "AttendanceSurveyAnswerFactory" - {
        mockkObject(Clock.System)
        every {
            Clock.System.now()
        } returns Instant.parse(
            "2022-01-01T00:00:00+09:00",
        )

        val surveyRepository: AttendanceSurveyRepositoryInterface = mockk()
        val factory = AttendanceSurveyAnswerFactory(surveyRepository)
        val fakeOpenCampus = AttendanceSurvey(
            name = "テスト",
            openCampusSchedule = OpenCampusDates(
                setOf(
                    OpenCampusDate(LocalDate(2022, 4, 1))
                ),
            ),
        )

        "生成" - {
            "正常系" {
                val cast: Cast = mockk(relaxed = true)
                every { cast.inSchool(any()) } returns true
                coEvery { surveyRepository.findById(any()) } returns fakeOpenCampus

                shouldNotThrowAny {
                    factory(
                        mockk(relaxed = true),
                        cast = cast,
                        mockk(relaxed = true),
                    )
                }
            }
            "異常系" - {
                "アンケートが見つからない場合、Err(AttendanceSurveyAnswerFactoryException.NotFoundSurvey()を返す" {
                    coEvery { surveyRepository.findById(any()) } returns null
                    val actual = factory(
                        mockk(relaxed = true),
                        mockk(relaxed = true), mockk(relaxed = true),
                    )
                    actual.shouldBeInstanceOf<Err<AttendanceSurveyAnswerFactoryException.NotFoundSurvey>>()
                }
                "アンケートが回答受付を終了している場合、Err(AttendanceSurveyAnswerFactoryException.NotAvailableSurvey())を返す" {
                    coEvery {
                        surveyRepository.findById(any())
                    } returns fakeOpenCampus.changeAvailable(false)

                    val actual = factory(
                        mockk(relaxed = true),
                        mockk(relaxed = true), mockk(relaxed = true),
                    )
                    actual.shouldBeInstanceOf<Err<AttendanceSurveyAnswerFactoryException.NotAvailableSurvey>>()
                }
                "アンケート内のオープンキャンパス開催日に学生が在籍していない場合、IllegalArgumentExceptionを投げる" {
                    val cast: Cast = mockk(relaxed = true)
                    every { cast.inSchool(any()) } returns false
                    coEvery { surveyRepository.findById(any()) } returns fakeOpenCampus

                    shouldThrow<IllegalArgumentException> {
                        factory(
                            mockk(relaxed = true),
                            cast = cast,
                            availableDays = OpenCampusDates(
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
                    }
                }
                "アンケート内のオープンキャンパス開催日以外の日にちが出勤可能日にある場合、IllegalArgumentExceptionを投げる" {
                    coEvery { surveyRepository.findById(any()) } returns fakeOpenCampus

                    shouldThrow<IllegalArgumentException> {
                        factory(
                            mockk(relaxed = true),
                            cast = mockk(relaxed = true),
                            mockk(relaxed = true),
                        )
                    }
                }
            }
        }
        unmockkObject(Clock.System)
    }
})

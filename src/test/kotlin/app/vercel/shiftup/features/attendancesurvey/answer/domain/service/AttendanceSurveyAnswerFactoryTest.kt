package app.vercel.shiftup.features.attendancesurvey.answer.domain.service

import app.vercel.shiftup.features.attendancesurvey.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendancesurvey.domain.model.value.OpenCampusDates
import app.vercel.shiftup.features.attendancesurvey.survey.domain.model.AttendanceSurvey
import app.vercel.shiftup.features.attendancesurvey.survey.domain.service.AttendanceSurveyRepositoryInterface
import app.vercel.shiftup.features.core.domain.model.nowTokyoLocalDateTime
import app.vercel.shiftup.features.user.account.domain.model.Cast
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate

class AttendanceSurveyAnswerFactoryTest : FreeSpec({
    "AttendanceSurveyAnswerFactory" - {
        mockkStatic("app.vercel.shiftup.features.core.domain.model.NowTokyoLocalDateTimeKt")
        every {
            Clock.System.nowTokyoLocalDateTime().date
        } returns LocalDate(2022, 1, 1)

        val surveyRepository: AttendanceSurveyRepositoryInterface = mockk()
        val factory = AttendanceSurveyAnswerFactory(surveyRepository)
        val fakeOpenCampus = AttendanceSurvey.of(
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
                "アンケートが見つからない場合、IllegalArgumentExceptionを投げる" {
                    coEvery { surveyRepository.findById(any()) } returns null
                    shouldThrow<IllegalArgumentException> {
                        factory(
                            mockk(relaxed = true),
                            mockk(relaxed = true), mockk(relaxed = true),
                        )
                    }
                }
                "アンケートが回答受付を終了している場合、IllegalArgumentExceptionを投げる" {
                    coEvery {
                        surveyRepository.findById(any())
                    } returns fakeOpenCampus.changeAvailable(false)

                    shouldThrow<IllegalArgumentException> {
                        factory(
                            mockk(relaxed = true),
                            mockk(relaxed = true),
                            mockk(relaxed = true),
                        )
                    }
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
    }
})

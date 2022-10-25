package app.vercel.shiftup.features.attendancesurvey.survey.domain.model

import app.vercel.shiftup.features.attendancesurvey.answer.domain.model.AttendanceSurveyAnswer
import app.vercel.shiftup.features.attendancesurvey.domain.model.value.AttendanceSurveyId
import app.vercel.shiftup.features.attendancesurvey.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendancesurvey.domain.model.value.OpenCampusDates
import app.vercel.shiftup.features.attendancesurvey.survey.domain.model.value.AttendanceSurveyAnswers
import app.vercel.shiftup.features.attendancesurvey.survey.domain.model.value.OpenCampus
import app.vercel.shiftup.features.core.domain.model.nowTokyoLocalDateTime
import app.vercel.shiftup.features.user.account.domain.model.CastId
import app.vercel.shiftup.features.user.account.domain.model.UserId
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

class AttendanceSurveyTest : FreeSpec({
    "AttendanceSurvey" - {
        mockkStatic("app.vercel.shiftup.features.core.domain.model.NowTokyoLocalDateTimeKt")
        every {
            Clock.System.nowTokyoLocalDateTime().date
        } returns LocalDate(2022, 1, 1)

        "生成" - {
            "正常系" - {
                "名前が空白文字や空ではなく、日程が全て同じ年度で呼び出し日以降の場合、生成できる" {
                    shouldNotThrowAny {
                        AttendanceSurvey.of(
                            name = "テスト",
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
                    shouldThrowExactly<IllegalArgumentException> {
                        AttendanceSurvey.of(
                            name = "テスト",
                            openCampusSchedule = OpenCampusDates(emptySet()),
                        )
                    }
                }
                "オープンキャンパスの日程が現在より古い場合、IllegalArgumentExceptionを投げる" {
                    shouldThrowExactly<IllegalArgumentException> {
                        AttendanceSurvey.of(
                            name = "テスト",
                            openCampusSchedule = OpenCampusDates(
                                setOf(OpenCampusDate(LocalDate(year = 2021, monthNumber = 12, dayOfMonth = 31)))
                            ),
                        )
                    }
                }
                "名前が空の場合、IllegalArgumentExceptionを投げる" {
                    shouldThrowExactly<IllegalArgumentException> {
                        AttendanceSurvey.of(
                            name = "",
                            openCampusSchedule = mockk(relaxed = true),
                        )
                    }
                }
                "名前が空白文字のみの場合、IllegalArgumentExceptionを投げる" {
                    shouldThrowExactly<IllegalArgumentException> {
                        AttendanceSurvey.of(
                            name = " ",
                            openCampusSchedule = mockk(relaxed = true),
                        )
                    }
                }
            }
        }
        "集計" - {
            "正常系" - {
                val openCampusDatesValue = setOf(
                    OpenCampusDate(LocalDate(2022, 1, 15)),
                    OpenCampusDate(LocalDate(2022, 1, 16)),
                    OpenCampusDate(LocalDate(2022, 1, 20)),
                    OpenCampusDate(LocalDate(2022, 1, 31)),
                )
                val survey = AttendanceSurvey.of(
                    name = "テスト",
                    openCampusSchedule = OpenCampusDates(openCampusDatesValue),
                )

                "回答が無い場合、キャスト一覧は空になる" {
                    val actual = AttendanceSurveyAnswers(emptySet()).let(survey::tally)
                    val expected = OpenCampusDates(openCampusDatesValue).map(::OpenCampus)

                    actual shouldBe expected
                }

                "回答がある場合、キャスト一覧にCastIdが保存される" {
                    val actual = setOf(
                        AttendanceSurveyAnswer.fromFactory(
                            surveyId = survey.id,
                            availableCastId = CastId(UserId("A")),
                            availableDays = OpenCampusDates(emptySet())
                        ),
                        AttendanceSurveyAnswer.fromFactory(
                            surveyId = survey.id,
                            availableCastId = CastId(UserId("B")),
                            availableDays = OpenCampusDates(
                                setOf(
                                    openCampusDatesValue.elementAt(0),
                                    openCampusDatesValue.elementAt(1),
                                    openCampusDatesValue.elementAt(3),
                                )
                            ),
                        ),
                        AttendanceSurveyAnswer.fromFactory(
                            surveyId = survey.id,
                            availableCastId = CastId(UserId("C")),
                            availableDays = OpenCampusDates(
                                setOf(
                                    openCampusDatesValue.elementAt(1),
                                )
                            ),
                        ),
                        AttendanceSurveyAnswer.fromFactory(
                            surveyId = survey.id,
                            availableCastId = CastId(UserId("D")),
                            availableDays = OpenCampusDates(
                                setOf(
                                    openCampusDatesValue.elementAt(0),
                                    openCampusDatesValue.elementAt(3),
                                )
                            ),
                        ),
                    ).let { survey.tally(AttendanceSurveyAnswers(it)) }

                    val openCampusConstructor = OpenCampus::class.primaryConstructor!!.apply {
                        isAccessible = true
                    }
                    val expected = setOf(
                        openCampusConstructor.call(
                            openCampusDatesValue.elementAt(0),
                            setOf(CastId(UserId("B")), CastId(UserId("D")))
                        ),
                        openCampusConstructor.call(
                            openCampusDatesValue.elementAt(1),
                            setOf(CastId(UserId("B")), CastId(UserId("C")))
                        ),
                        openCampusConstructor.call(
                            openCampusDatesValue.elementAt(2),
                            emptySet<CastId>(),
                        ),
                        openCampusConstructor.call(
                            openCampusDatesValue.elementAt(3),
                            setOf(CastId(UserId("B")), CastId(UserId("D"))),
                        )
                    )

                    actual shouldBe expected
                }
            }
        }
        "異常系" - {
            "回答のsurveyIdがアンケートと異なる場合、IllegalArgumentExceptionを投げる" {
                val survey = AttendanceSurvey.of(
                    name = "テスト",
                    openCampusSchedule = OpenCampusDates(
                        setOf(
                            OpenCampusDate(LocalDate(2022, 1, 15)),
                        ),
                    ),
                )

                val answers = AttendanceSurveyAnswers(
                    setOf(
                        AttendanceSurveyAnswer.fromFactory(
                            availableCastId = mockk(relaxed = true),
                            availableDays = mockk(relaxed = true),
                            surveyId = AttendanceSurveyId(""),
                        ),
                    )
                )
                shouldThrowExactly<IllegalArgumentException> {
                    survey.tally(answers)
                }
            }
        }
    }
})

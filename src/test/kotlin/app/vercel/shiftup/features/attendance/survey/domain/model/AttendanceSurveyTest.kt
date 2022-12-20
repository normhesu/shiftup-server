package app.vercel.shiftup.features.attendance.survey.domain.model

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.survey.domain.model.value.AttendanceSurveyAnswer
import app.vercel.shiftup.features.attendance.survey.domain.model.value.AttendanceSurveyAnswers
import app.vercel.shiftup.features.attendance.survey.domain.model.value.OpenCampus
import app.vercel.shiftup.features.attendance.survey.domain.model.value.OpenCampusDates
import app.vercel.shiftup.features.user.account.domain.model.CastId
import app.vercel.shiftup.features.user.account.domain.model.UserId
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.annotation.DoNotParallelize
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

@DoNotParallelize
class AttendanceSurveyTest : FreeSpec({
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

    "AttendanceSurvey" - {
        "生成" - {
            "正常系" - {
                "名前が空白文字や空ではなく、日程が全て同じ年度で呼び出し日以降の場合、生成できる" {
                    shouldNotThrowAny {
                        AttendanceSurvey(
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
                        AttendanceSurvey(
                            name = "テスト",
                            openCampusSchedule = OpenCampusDates(emptySet()),
                        )
                    }
                }
                "オープンキャンパスの日程が現在より古い場合、IllegalArgumentExceptionを投げる" {
                    shouldThrowExactly<IllegalArgumentException> {
                        AttendanceSurvey(
                            name = "テスト",
                            openCampusSchedule = OpenCampusDates(
                                setOf(OpenCampusDate(LocalDate(2021, 12, 31)))
                            ),
                        )
                    }
                }
                "名前が空の場合、IllegalArgumentExceptionを投げる" {
                    shouldThrowExactly<IllegalArgumentException> {
                        AttendanceSurvey(
                            name = "",
                            openCampusSchedule = mockk(relaxed = true),
                        )
                    }
                }
                "名前が空白文字のみの場合、IllegalArgumentExceptionを投げる" {
                    shouldThrowExactly<IllegalArgumentException> {
                        AttendanceSurvey(
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
                val survey = AttendanceSurvey(
                    name = "テスト",
                    openCampusSchedule = OpenCampusDates(openCampusDatesValue),
                )

                "回答が無い場合、キャスト一覧は空になる" {
                    survey.answers shouldBe AttendanceSurveyAnswers.empty(survey.id)
                    survey.tally() shouldBe OpenCampusDates(openCampusDatesValue).map(::OpenCampus)
                }

                "回答がある場合、キャスト一覧にCastIdが保存される" {
                    val actual = run {
                        val answers = setOf(
                            AttendanceSurveyAnswer.fromFactory(
                                surveyId = survey.id,
                                availableCastId = CastId.reconstruct(UserId("A")),
                                availableDays = OpenCampusDates(emptySet())
                            ),
                            AttendanceSurveyAnswer.fromFactory(
                                surveyId = survey.id,
                                availableCastId = CastId.reconstruct(UserId("B")),
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
                                availableCastId = CastId.reconstruct(UserId("C")),
                                availableDays = OpenCampusDates(
                                    setOf(
                                        openCampusDatesValue.elementAt(1),
                                    )
                                ),
                            ),
                            AttendanceSurveyAnswer.fromFactory(
                                surveyId = survey.id,
                                availableCastId = CastId.reconstruct(UserId("D")),
                                availableDays = OpenCampusDates(
                                    setOf(
                                        openCampusDatesValue.elementAt(0),
                                        openCampusDatesValue.elementAt(3),
                                    )
                                ),
                            ),
                        )

                        answers.fold(survey) { survey, answer ->
                            survey.addOrReplaceAnswer(answer)
                        }.tally()
                    }

                    val expected = run {
                        val openCampusConstructor = OpenCampus::class.primaryConstructor!!.apply {
                            isAccessible = true
                        }

                        setOf(
                            openCampusConstructor.call(
                                openCampusDatesValue.elementAt(0),
                                setOf("B", "D").map { CastId.reconstruct(UserId(it)) }.toSet()
                            ),
                            openCampusConstructor.call(
                                openCampusDatesValue.elementAt(1),
                                setOf("B", "C").map { CastId.reconstruct(UserId(it)) }.toSet()
                            ),
                            openCampusConstructor.call(
                                openCampusDatesValue.elementAt(2),
                                emptySet<CastId>(),
                            ),
                            openCampusConstructor.call(
                                openCampusDatesValue.elementAt(3),
                                setOf("B", "D").map { CastId.reconstruct(UserId(it)) }.toSet()
                            )
                        )
                    }

                    actual shouldBe expected
                }
            }
        }
    }
})

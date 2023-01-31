package app.vercel.shiftup.features.attendance.survey.domain.model

import app.vercel.shiftup.features.attendance.domain.model.value.OpenCampusDate
import app.vercel.shiftup.features.attendance.survey.answer.domain.model.AttendanceSurveyAnswer
import app.vercel.shiftup.features.attendance.survey.domain.model.value.OpenCampus
import app.vercel.shiftup.features.attendance.survey.domain.model.value.SameAttendanceSurveyAnswers
import app.vercel.shiftup.features.attendance.survey.domain.model.value.SameFiscalYearOpenCampusDates
import app.vercel.shiftup.features.core.domain.model.toTokyoLocalDateTime
import app.vercel.shiftup.features.user.account.domain.model.CastId
import app.vercel.shiftup.features.user.account.domain.model.UserId
import io.kotest.core.annotation.DoNotParallelize
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
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
        "集計" - {
            "正常系" - {
                val openCampusDatesValue = setOf(
                    OpenCampusDate(LocalDate(2022, 1, 15)),
                    OpenCampusDate(LocalDate(2022, 1, 16)),
                    OpenCampusDate(LocalDate(2022, 1, 20)),
                    OpenCampusDate(LocalDate(2022, 1, 31)),
                )
                val survey = AttendanceSurvey.fromFactory(
                    name = "テスト",
                    openCampusSchedule = SameFiscalYearOpenCampusDates(openCampusDatesValue),
                    creationDate = Clock.System.now().toTokyoLocalDateTime().date,
                    available = true,
                    id = AttendanceSurveyId(),
                )

                "回答が無い場合、キャスト一覧は空になる" {
                    val actual = survey.tally(SameAttendanceSurveyAnswers.empty(survey.id))
                    val expected = SameFiscalYearOpenCampusDates(openCampusDatesValue).map(::OpenCampus)
                    actual shouldBe expected
                }

                "回答がある場合、キャスト一覧にCastIdが保存される" {
                    val actual = run {
                        val answersSet = setOf(
                            AttendanceSurveyAnswer.fromFactory(
                                surveyId = survey.id,
                                availableCastId = CastId.unsafe(UserId("A")),
                                availableDays = SameFiscalYearOpenCampusDates.empty()
                            ),
                            AttendanceSurveyAnswer.fromFactory(
                                surveyId = survey.id,
                                availableCastId = CastId.unsafe(UserId("B")),
                                availableDays = SameFiscalYearOpenCampusDates(
                                    setOf(
                                        openCampusDatesValue.elementAt(0),
                                        openCampusDatesValue.elementAt(1),
                                        openCampusDatesValue.elementAt(3),
                                    )
                                ),
                            ),
                            AttendanceSurveyAnswer.fromFactory(
                                surveyId = survey.id,
                                availableCastId = CastId.unsafe(UserId("C")),
                                availableDays = SameFiscalYearOpenCampusDates(
                                    setOf(
                                        openCampusDatesValue.elementAt(1),
                                    )
                                ),
                            ),
                            AttendanceSurveyAnswer.fromFactory(
                                surveyId = survey.id,
                                availableCastId = CastId.unsafe(UserId("D")),
                                availableDays = SameFiscalYearOpenCampusDates(
                                    setOf(
                                        openCampusDatesValue.elementAt(0),
                                        openCampusDatesValue.elementAt(3),
                                    )
                                ),
                            ),
                        )

                        survey.tally(
                            SameAttendanceSurveyAnswers(
                                answers = answersSet,
                                surveyId = survey.id,
                            ),
                        )
                    }

                    val expected = run {
                        val openCampusConstructor = OpenCampus::class.primaryConstructor!!.apply {
                            isAccessible = true
                        }

                        setOf(
                            openCampusConstructor.call(
                                openCampusDatesValue.elementAt(0),
                                setOf("B", "D").map { CastId.unsafe(UserId(it)) }.toSet()
                            ),
                            openCampusConstructor.call(
                                openCampusDatesValue.elementAt(1),
                                setOf("B", "C").map { CastId.unsafe(UserId(it)) }.toSet()
                            ),
                            openCampusConstructor.call(
                                openCampusDatesValue.elementAt(2),
                                emptySet<CastId>(),
                            ),
                            openCampusConstructor.call(
                                openCampusDatesValue.elementAt(3),
                                setOf("B", "D").map { CastId.unsafe(UserId(it)) }.toSet()
                            )
                        )
                    }

                    actual shouldBe expected
                }
            }
        }
    }
})

package app.vercel.shiftup.features.user.domain.model.value

import app.vercel.shiftup.features.core.domain.model.nowTokyoLocalDateTime
import io.kotest.core.spec.style.FreeSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime

class EntranceYearTest : FreeSpec({
    "EntranceYear" - {
        "getSchoolYear" - {
            mockkStatic("app.vercel.shiftup.features.core.domain.model.NowTokyoLocalDateTimeKt")

            data class Params(
                val currentYear: Int,
                val currentMonth: Int,
                val entranceYearValue: Int,
                val tenureValue: Int,
                val expectedSchoolYearValue: Int?,
            )

            fun invokeTest(vararg params: Params) {
                params.forAll {
                    every {
                        Clock.System.nowTokyoLocalDateTime()
                    } returns LocalDateTime(
                        it.currentYear, it.currentMonth,
                        1, 0, 0,
                    )

                    EntranceYear(it.entranceYearValue).getSchoolYear(
                        tenure = Tenure(it.tenureValue),
                    ) shouldBe it
                        .expectedSchoolYearValue
                        ?.let(::SchoolYear)
                }
            }

            "在学中の場合は学年を返す" {
                invokeTest(
                    Params(
                        currentYear = 2022, currentMonth = 3,
                        entranceYearValue = 2021, tenureValue = 2,
                        expectedSchoolYearValue = 1,
                    ),
                    Params(
                        currentYear = 2022, currentMonth = 4,
                        entranceYearValue = 2021, tenureValue = 2,
                        expectedSchoolYearValue = 2,
                    ),
                    Params(
                        currentYear = 2023, currentMonth = 4,
                        entranceYearValue = 2021, tenureValue = 4,
                        expectedSchoolYearValue = 3,
                    ),
                    Params(
                        currentYear = 2024, currentMonth = 4,
                        entranceYearValue = 2021, tenureValue = 4,
                        expectedSchoolYearValue = 4,
                    ),
                )
            }

            "入学前の場合はnullを返す" {
                invokeTest(
                    Params(
                        currentYear = 2021, currentMonth = 3,
                        entranceYearValue = 2021, tenureValue = 2,
                        expectedSchoolYearValue = null,
                    ),
                    Params(
                        currentYear = 2021, currentMonth = 3,
                        entranceYearValue = 2021, tenureValue = 3,
                        expectedSchoolYearValue = null,
                    ),
                    Params(
                        currentYear = 2021, currentMonth = 4,
                        entranceYearValue = 2022, tenureValue = 4,
                        expectedSchoolYearValue = null,
                    ),
                )
            }

            "卒業後の場合はnullを返す" {
                invokeTest(
                    Params(
                        currentYear = 2023, currentMonth = 4,
                        entranceYearValue = 2021, tenureValue = 2,
                        expectedSchoolYearValue = null,
                    ),
                    Params(
                        currentYear = 2024, currentMonth = 4,
                        entranceYearValue = 2021, tenureValue = 3,
                        expectedSchoolYearValue = null,
                    ),
                    Params(
                        currentYear = 2025, currentMonth = 4,
                        entranceYearValue = 2021, tenureValue = 4,
                        expectedSchoolYearValue = null,
                    ),
                )
            }
        }
    }
})

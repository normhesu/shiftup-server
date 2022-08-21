package app.vercel.shiftup.features.user.account.domain.model.value

import app.vercel.shiftup.features.user.domain.model.value.EntranceYear
import io.kotest.core.spec.style.FreeSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockkStatic
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class SchoolYearTest : FreeSpec({
    "生成" - {
        mockkStatic(ZonedDateTime::class)

        data class Params(
            val currentYear: Int,
            val currentMonth: Int,
            val entranceYearValue: Int,
            val tenure: Int,
            val expectedSchoolYearValue: Int?,
        )

        fun invokeTest(vararg params: Params) {
            val tokyoZoneId = ZoneId.of("Asia/Tokyo")
            params.forAll {
                every {
                    ZonedDateTime.now(tokyoZoneId)
                } returns ZonedDateTime.of(
                    LocalDateTime.of(
                        it.currentYear, it.currentMonth,
                        1, 0, 0,
                    ),
                    tokyoZoneId,
                )

                SchoolYear.of(
                    entranceYear = EntranceYear(it.entranceYearValue),
                    tenure = it.tenure,
                )?.value shouldBe it.expectedSchoolYearValue
            }
        }

        "在学中の場合は学年を返す" {
            invokeTest(
                Params(
                    currentYear = 2022, currentMonth = 3,
                    entranceYearValue = 2021, tenure = 2,
                    expectedSchoolYearValue = 1,
                ),
                Params(
                    currentYear = 2022, currentMonth = 4,
                    entranceYearValue = 2021, tenure = 2,
                    expectedSchoolYearValue = 2,
                ),
                Params(
                    currentYear = 2023, currentMonth = 4,
                    entranceYearValue = 2021, tenure = 4,
                    expectedSchoolYearValue = 3,
                ),
                Params(
                    currentYear = 2024, currentMonth = 4,
                    entranceYearValue = 2021, tenure = 4,
                    expectedSchoolYearValue = 4,
                ),
            )
        }

        "入学前の場合はnullを返す" {
            invokeTest(
                Params(
                    currentYear = 2021, currentMonth = 3,
                    entranceYearValue = 2021, tenure = 2,
                    expectedSchoolYearValue = null,
                ),
                Params(
                    currentYear = 2021, currentMonth = 3,
                    entranceYearValue = 2021, tenure = 3,
                    expectedSchoolYearValue = null,
                ),
                Params(
                    currentYear = 2021, currentMonth = 4,
                    entranceYearValue = 2022, tenure = 4,
                    expectedSchoolYearValue = null,
                ),
            )
        }

        "卒業後の場合はnullを返す" {
            invokeTest(
                Params(
                    currentYear = 2023, currentMonth = 4,
                    entranceYearValue = 2021, tenure = 2,
                    expectedSchoolYearValue = null,
                ),
                Params(
                    currentYear = 2024, currentMonth = 4,
                    entranceYearValue = 2021, tenure = 3,
                    expectedSchoolYearValue = null,
                ),
                Params(
                    currentYear = 2025, currentMonth = 4,
                    entranceYearValue = 2021, tenure = 4,
                    expectedSchoolYearValue = null,
                ),
            )
        }
    }
})

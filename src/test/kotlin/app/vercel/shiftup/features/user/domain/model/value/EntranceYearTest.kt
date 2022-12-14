package app.vercel.shiftup.features.user.domain.model.value

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FreeSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class EntranceYearTest : FreeSpec({
    isolationMode = IsolationMode.SingleInstance

    beforeSpec {
        mockkObject(Clock.System)
    }

    afterSpec {
        unmockkObject(Clock.System)
    }

    "EntranceYear" - {
        "学年を取得" - {
            fun mockNowTime(yearAndMonth: Pair<Int, Int>) {
                val (year, month) = yearAndMonth
                val currentMonthText = month.toString().padStart(2, '0')
                every {
                    Clock.System.now()
                } returns Instant.parse(
                    "$year-$currentMonthText-01T00:00:00+09:00",
                )
            }

            "在学中の場合は学年を返す" {
                data class Params(
                    val mockYearAndMonth: Pair<Int, Int>,
                    val entranceYearAndTenure: Pair<Int, Int>,
                    val expectedSchoolYear: Int?,
                )

                listOf(
                    Params(
                        mockYearAndMonth = 2022 to 3,
                        entranceYearAndTenure = 2021 to 2,
                        expectedSchoolYear = 1,
                    ),
                    Params(
                        mockYearAndMonth = 2022 to 4,
                        entranceYearAndTenure = 2021 to 2,
                        expectedSchoolYear = 2,
                    ),
                    Params(
                        mockYearAndMonth = 2023 to 4,
                        entranceYearAndTenure = 2021 to 4,
                        expectedSchoolYear = 3,
                    ),
                    Params(
                        mockYearAndMonth = 2024 to 4,
                        entranceYearAndTenure = 2021 to 4,
                        expectedSchoolYear = 4,
                    ),
                ).forAll {
                    mockNowTime(it.mockYearAndMonth)

                    val (entranceYear, tenure) = it.entranceYearAndTenure
                    val actual = EntranceYear(entranceYear).getSchoolYear(Tenure(tenure))
                    val expected = it.expectedSchoolYear?.let(::SchoolYear)
                    actual shouldBe expected
                }
            }

            "入学前の場合はnullを返す" {
                data class Params(
                    val mockYearAndMonth: Pair<Int, Int>,
                    val entranceYearAndTenure: Pair<Int, Int>,
                )

                listOf(
                    Params(
                        mockYearAndMonth = 2021 to 3,
                        entranceYearAndTenure = 2021 to 2,
                    ),
                    Params(
                        mockYearAndMonth = 2021 to 3,
                        entranceYearAndTenure = 2021 to 3,
                    ),
                    Params(
                        mockYearAndMonth = 2021 to 4,
                        entranceYearAndTenure = 2022 to 4,
                    ),
                ).forAll {
                    mockNowTime(it.mockYearAndMonth)

                    val (entranceYear, tenure) = it.entranceYearAndTenure
                    val actual = EntranceYear(entranceYear).getSchoolYear(Tenure(tenure))
                    actual shouldBe null
                }
            }

            "卒業後の場合はnullを返す" {
                data class Params(
                    val mockYearAndMonth: Pair<Int, Int>,
                    val entranceYearAndTenure: Pair<Int, Int>,
                )

                listOf(
                    Params(
                        mockYearAndMonth = 2023 to 4,
                        entranceYearAndTenure = 2021 to 2,
                    ),
                    Params(
                        mockYearAndMonth = 2024 to 4,
                        entranceYearAndTenure = 2021 to 3,
                    ),
                    Params(
                        mockYearAndMonth = 2025 to 4,
                        entranceYearAndTenure = 2021 to 4,
                    ),
                ).forAll {
                    mockNowTime(it.mockYearAndMonth)

                    val (entranceYear, tenure) = it.entranceYearAndTenure
                    val actual = EntranceYear(entranceYear).getSchoolYear(Tenure(tenure))
                    actual shouldBe null
                }
            }
        }
    }
})

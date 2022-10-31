package app.vercel.shiftup.features.user.domain.model.value

import io.kotest.core.spec.style.FreeSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.forAll

class StudentNumberTest : FreeSpec({
    "StudentNumber" - {
        "学年を取得" - {
            "日本工学院八王子専門学校" {
                forAll(Arb.int(0..999)) { entranceYearValue ->
                    val studentNumber = StudentNumber(
                        "G${"$entranceYearValue".padStart(3, '0')}C0000",
                    )
                    val entranceYear = EntranceYear(
                        entranceYearValue + 2000,
                    )
                    val tenure = Tenure(2)
                    studentNumber.getSchoolYear(tenure) == entranceYear.getSchoolYear(tenure)
                }
            }
            "東京工科大学" {
                forAll(Arb.int(0..99)) { entranceYearValue ->
                    val studentNumber = StudentNumber(
                        "C0A${"$entranceYearValue".padStart(2, '0')}999",
                    )
                    val entranceYear = EntranceYear(
                        entranceYearValue + 2000,
                    )
                    val tenure = Tenure(4)
                    studentNumber.getSchoolYear(tenure) == entranceYear.getSchoolYear(tenure)
                }
            }
        }
    }
})

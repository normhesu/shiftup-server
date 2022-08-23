package app.vercel.shiftup.features.user.domain.model.value

import io.kotest.core.spec.style.FreeSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.forAll

class StudentNumberTest : FreeSpec({
    "学年を取得" {
        forAll(Arb.int(0..999)) { entranceYearValue ->
            val studentNumber = StudentNumber(
                "G${"$entranceYearValue".padStart(3, '0')}C0000",
            )
            val entranceYear = EntranceYear(
                entranceYearValue + 2000,
            )
            val tenure = Department.C2.tenure
            studentNumber.getSchoolYear(tenure) == entranceYear.getSchoolYear(tenure)
        }
    }
})

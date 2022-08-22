package app.vercel.shiftup.features.user.domain.model.value

import io.kotest.core.spec.style.FreeSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.forAll

class StudentNumberTest : FreeSpec({
    "entranceYear取得" {
        forAll(Arb.int(0..999)) {
            val studentNumber = StudentNumber(
                "G${"$it".padStart(3, '0')}C0000",
            )
            studentNumber.entranceYear == EntranceYear(it + 2000)
        }
    }
})

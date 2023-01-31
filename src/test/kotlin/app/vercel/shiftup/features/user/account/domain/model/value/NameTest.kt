package app.vercel.shiftup.features.user.account.domain.model.value

import io.kotest.core.spec.style.FreeSpec
import io.kotest.data.row
import io.kotest.inspectors.forAll
import io.kotest.matchers.shouldBe

class NameTest : FreeSpec({
    "Name" - {
        "laxEquals" {
            listOf(
                row("苗字名前", "苗字名前", true),
                row("苗苗字名前", "苗字名前", false),
                row(" 苗字　	名前　", "苗字名前", true),
                row(" 苗字　	名名前　", "苗字名前", false),
                row("苗字 名前", "苗字名前", true),
                row("苗字　名前", "苗字名前", true),
                row("苗字 名前", "苗字 名前", true),
                row("苗字　名前", "苗字　名前", true),
                row("苗字 名前", "苗字　名前", true),
                row("苗字 名前", "苗字名 前", false),
                row("苗字 名前", "苗字名　前", false),
                row("苗　字名前", "苗字名 前", false),
            ).forAll { (nameString1, nameString2, result) ->
                (Name(nameString1) laxEquals Name(nameString2)) shouldBe result
            }
        }
    }
})

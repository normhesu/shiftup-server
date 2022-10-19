package app.vercel.shiftup.features.user.domain.model.value

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class StudentNumber(private val value: String) {
    init {
        require(value matches regex)
    }

    fun lowercaseValue() = value.lowercase()

    private val entranceYear
        get() = EntranceYear(
            ("2" + value.substring(startIndex = 1, endIndex = 4)).toInt(),
        )

    fun getSchoolYear(
        tenure: Tenure,
        fiscalYear: Int? = null,
    ) = entranceYear.getSchoolYear(
        tenure = tenure,
        fiscalYear = fiscalYear,
    )
}

private val regex = Regex("G\\d{3}[A-Z]\\d{4}")

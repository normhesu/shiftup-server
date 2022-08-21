package app.vercel.shiftup.features.user.domain.model.value

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class StudentNumber(val value: String) {
    init {
        require(value matches regex)
    }

    fun lowercase() = value.lowercase()

    val entranceYear
        get() = EntranceYear(
            ("2" + value.substring(startIndex = 1, endIndex = 4)).toInt(),
        )
}

private val regex = Regex("G\\d{3}[A-Z]\\d{4}")

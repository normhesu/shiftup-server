package app.vercel.shiftup.features.user.domain.model.value

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(StudentNumberSerializer::class)
sealed interface StudentNumber {
    fun lowercaseValue(): String
    fun getSchoolYear(tenure: Tenure, fiscalYear: Int? = null): SchoolYear?

    companion object {
        operator fun invoke(value: String): StudentNumber = sequenceOf(::TeuStudentNumber, ::NeecStudentNumber)
            .map { runCatching { it(value) } }
            .find { it.isSuccess }
            .let {
                requireNotNull(it) {
                    "日本工学院八王子専門学校か東京工科大学の学籍番号である必要があります"
                }
            }
            .getOrThrow()
    }
}

@Serializable
@JvmInline
value class NeecStudentNumber(private val value: String) : StudentNumber {
    init {
        require(value matches regex)
    }

    companion object {
        private val regex = Regex("G\\d{3}[A-Z]\\d{4}")
    }

    override fun lowercaseValue() = value.lowercase()

    private val entranceYear
        get() = EntranceYear(
            ("2" + value.substring(startIndex = 1, endIndex = 4)).toInt(),
        )

    override fun getSchoolYear(
        tenure: Tenure,
        fiscalYear: Int?,
    ) = entranceYear.getSchoolYear(
        tenure = tenure,
        fiscalYear = fiscalYear,
    )

    override fun toString() = value
}

@Serializable
@JvmInline
value class TeuStudentNumber(private val value: String) : StudentNumber {
    init {
        require(value matches regex)
    }

    companion object {
        const val digits = 8
        private val regex = Regex("[\\dA-Z]{3}\\d{5}")
    }

    override fun lowercaseValue() = value.lowercase()

    private val entranceYear
        get() = EntranceYear(
            ("20" + value.substring(startIndex = 3, endIndex = 5)).toInt(),
        )

    override fun getSchoolYear(
        tenure: Tenure,
        fiscalYear: Int?,
    ) = entranceYear.getSchoolYear(
        tenure = tenure,
        fiscalYear = fiscalYear,
    )

    override fun toString() = value
}

object StudentNumberSerializer : KSerializer<StudentNumber> {
    override val descriptor = PrimitiveSerialDescriptor(
        serialName = "app.vercel.shiftup.features.user.domain.model.value.StudentNumberSerializer",
        kind = PrimitiveKind.STRING,
    )

    override fun serialize(encoder: Encoder, value: StudentNumber) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): StudentNumber {
        return StudentNumber(decoder.decodeString())
    }
}

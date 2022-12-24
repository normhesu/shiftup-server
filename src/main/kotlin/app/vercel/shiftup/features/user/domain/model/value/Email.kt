package app.vercel.shiftup.features.user.domain.model.value

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(EmailSerializer::class)
sealed interface Email {
    val studentNumber: StudentNumber

    companion object {
        operator fun invoke(value: String): Email = sequenceOf(::TeuEmail, ::NeecEmail)
            .map { runCatching { it(value) } }
            .find { it.isSuccess }
            .let {
                requireNotNull(it) {
                    "日本工学院八王子専門学校か東京工科大学のメールアドレスである必要があります"
                }
            }
            .getOrThrow()
    }
}

@Serializable
@JvmInline
value class NeecEmail(private val value: String) : Email {
    init {
        require(value.endsWith(suffix))
    }

    companion object {
        private const val suffix = "@g.neec.ac.jp"
    }

    override val studentNumber: NeecStudentNumber
        get() = NeecStudentNumber(
            value.removeSuffix(suffix).uppercase(),
        )

    override fun toString() = value
}

@Serializable
@JvmInline
value class TeuEmail(private val value: String) : Email {
    init {
        require(value.endsWith(suffix))
    }

    companion object {
        private const val suffix = "@edu.teu.ac.jp"
    }

    override val studentNumber: TeuStudentNumber
        get() = TeuStudentNumber(
            value.take(TeuStudentNumber.digits).uppercase()
        )

    override fun toString() = value
}

object EmailSerializer : KSerializer<Email> {
    override val descriptor = PrimitiveSerialDescriptor(
        serialName = "EmailSerializer",
        kind = PrimitiveKind.STRING,
    )

    override fun serialize(encoder: Encoder, value: Email) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Email {
        return Email(decoder.decodeString())
    }
}

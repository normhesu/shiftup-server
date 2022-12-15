package app.vercel.shiftup.features.attendance.request.domain.model.value

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(AttendanceRequestStateSerializer::class)
@Suppress("unused")
sealed interface AttendanceRequestState {
    val name: Name

    companion object {
        operator fun invoke(name: Name) = when (name) {
            Name.Blank -> Blank
            Name.Accepted -> Accepted
            Name.Declined -> Declined
        }
    }

    @Serializable
    sealed interface NonBlank : AttendanceRequestState {

        companion object {
            operator fun invoke(name: Name): NonBlank {
                val state = AttendanceRequestState(name)
                require(state is NonBlank)
                return state
            }
        }
    }

    @Serializable
    object Blank : AttendanceRequestState {
        override val name = Name.Blank
    }

    @Serializable
    object Accepted : NonBlank {
        override val name = Name.Accepted
    }

    @Serializable
    object Declined : NonBlank {
        override val name = Name.Declined
    }

    @Serializable
    enum class Name {
        Blank, Accepted, Declined
    }
}

// パッケージ名まで含めないようにする
object AttendanceRequestStateSerializer : KSerializer<AttendanceRequestState> {
    override val descriptor = PrimitiveSerialDescriptor(
        serialName = "AttendanceRequestStateSerializer",
        kind = PrimitiveKind.STRING,
    )

    override fun serialize(encoder: Encoder, value: AttendanceRequestState) {
        encoder.encodeString(value.name.name)
    }

    override fun deserialize(
        decoder: Decoder,
    ) = AttendanceRequestState(
        enumValueOf(decoder.decodeString())
    )
}

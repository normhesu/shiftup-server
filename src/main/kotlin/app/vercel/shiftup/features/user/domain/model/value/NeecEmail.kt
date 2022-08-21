package app.vercel.shiftup.features.user.domain.model.value

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class NeecEmail(val value: String) {
    init {
        require(value.endsWith(suffix))
    }

    companion object {
        private const val suffix = "@g.neec.ac.jp"

        fun of(studentNumber: StudentNumber): NeecEmail {
            return NeecEmail("${studentNumber.lowercase()}@g.neec.ac.jp")
        }
    }

    val studentNumber: StudentNumber
        get() = StudentNumber(
            value.removeSuffix(suffix).uppercase(),
        )
}

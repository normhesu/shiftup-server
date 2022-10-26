package app.vercel.shiftup.features.user.domain.model.value

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class NeecEmail(private val value: String) {
    init {
        require(value.endsWith(suffix))
    }

    companion object {
        private const val suffix = "@g.neec.ac.jp"

        operator fun invoke(studentNumber: StudentNumber): NeecEmail {
            return NeecEmail(studentNumber.lowercaseValue() + suffix)
        }
    }

    val studentNumber: StudentNumber
        get() = StudentNumber(
            value.removeSuffix(suffix).uppercase(),
        )
}

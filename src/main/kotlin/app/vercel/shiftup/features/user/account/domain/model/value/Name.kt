package app.vercel.shiftup.features.user.account.domain.model.value

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class Name(private val value: String) {
    init {
        require(value.isNotBlank())
    }
}

package app.vercel.shiftup.features.user.account.domain.model.value

import app.vercel.shiftup.features.user.domain.model.value.StudentNumber
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class Name(val value: String) {
    init {
        require(value.isNotBlank())
    }

    constructor(
        familyName: String,
        givenName: String,
    ) : this(
        value = getName(
            familyName = familyName,
            givenName = givenName,
        ),
    )
}

private fun getName(
    familyName: String,
    givenName: String,
): String {
    return arrayOf(
        familyName to givenName,
        givenName to familyName,
    ).find {
        runCatching {
            StudentNumber(it.first.trim())
        }.isSuccess
    }?.second ?: (familyName + givenName)
}

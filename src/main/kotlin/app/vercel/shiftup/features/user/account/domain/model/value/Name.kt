package app.vercel.shiftup.features.user.account.domain.model.value

import app.vercel.shiftup.features.user.domain.model.value.NeecStudentNumber
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class Name(private val value: String) {
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
): String = runCatching { NeecStudentNumber(familyName) }.fold(
    onSuccess = { givenName.replace(" ", "") },
    onFailure = { familyName },
)

package app.vercel.shiftup.features.user.account.domain.model.value

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class Name(private val value: String) {
    init {
        require(value.isNotBlank())
    }

    infix fun laxEquals(other: Name) = when {
        this.value.replaceWhitespaceToSpace() == other.value.replaceWhitespaceToSpace() -> true
        this.value.removeWhitespace() != other.value.removeWhitespace() -> false
        else -> {
            this.value.notContainsWhitespace() || other.value.notContainsWhitespace()
        }
    }
}

private fun String.replaceWhitespaceToSpace() = this.toCharArray().map {
    if (it.isWhitespace()) ' ' else it
}.joinToString("")

private fun String.removeWhitespace() = this.filter {
    it.isWhitespace().not()
}

private fun String.notContainsWhitespace() = this.any {
    it.isWhitespace()
}.not()

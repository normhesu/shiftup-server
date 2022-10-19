package app.vercel.shiftup.features.core.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month

fun LocalDate.fiscalYear() = when (month) {
    in Month.JANUARY..Month.MARCH -> year - 1
    else -> year
}

fun LocalDateTime.fiscalYear() = date.fiscalYear()

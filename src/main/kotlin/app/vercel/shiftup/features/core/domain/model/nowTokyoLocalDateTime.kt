package app.vercel.shiftup.features.core.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun Clock.System.nowTokyoLocalDateTime(): LocalDateTime {
    return now().toLocalDateTime(TimeZone.of("Asia/Tokyo"))
}

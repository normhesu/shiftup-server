package app.vercel.shiftup.features.core.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun Instant.toTokyoLocalDateTime() = toLocalDateTime(TimeZone.of("Asia/Tokyo"))

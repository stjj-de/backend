package de.stjj.backend.utils

import java.time.Instant
import java.time.LocalDateTime
import java.util.*

fun Instant.asLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(this, Calendar.getInstance().timeZone.toZoneId())
fun LocalDateTime.asInstant(): Instant = atZone(Calendar.getInstance().timeZone.toZoneId()).toInstant()

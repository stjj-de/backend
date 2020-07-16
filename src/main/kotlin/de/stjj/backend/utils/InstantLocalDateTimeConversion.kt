package de.stjj.backend.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

private val zoneOffset = ZoneOffset.ofHours(2)

fun Instant.asLocalDateTime() = LocalDateTime.ofInstant(this, zoneOffset)!!
fun LocalDateTime.asInstant() = toInstant(zoneOffset)!!

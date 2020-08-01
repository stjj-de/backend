package de.stjj.backend.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

// I really don't understand this, but sometimes it has to be 2 and sometimes 0
private val zoneOffset = ZoneOffset.ofHours(tzOffsetHours)

fun Instant.asLocalDateTime() = LocalDateTime.ofInstant(this, zoneOffset)!!
fun LocalDateTime.asInstant() = toInstant(zoneOffset)!!

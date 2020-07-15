package de.stjj.backend.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

fun Instant.asLocalDateTime() = LocalDateTime.ofInstant(this, ZoneOffset.ofHours(2))

package de.stjj.backend.utils

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.and

fun Iterable<Op<Boolean>>.combine() = reduce { acc, op -> acc and op }

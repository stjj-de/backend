package de.stjj.backend.utils

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and

fun List<Op<Boolean>>.combine() = if (size == 0) null else reduce { acc, op -> acc and op }

fun SqlExpressionBuilder.buildWhere(builder: MutableList<Op<Boolean>>.() -> Unit) = mutableListOf<Op<Boolean>>().apply(builder).combine()

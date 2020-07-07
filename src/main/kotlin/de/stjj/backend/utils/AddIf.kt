package de.stjj.backend.utils

fun <T> MutableList<T>.addIf(condition: Boolean, fn: () -> T): Boolean {
    if (condition) add(fn())
    return condition
}

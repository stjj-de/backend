package de.stjj.backend.utils

import io.jooby.Context
import kotlin.reflect.KClass

fun <T : Any> Context.body(kClass: KClass<T>) = body(kClass.java)

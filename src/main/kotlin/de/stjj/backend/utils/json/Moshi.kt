package de.stjj.backend.utils.json

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

fun <T> JsonAdapter<T>.withProjectOptions(): JsonAdapter<T> = serializeNulls().failOnUnknown().nullSafe()

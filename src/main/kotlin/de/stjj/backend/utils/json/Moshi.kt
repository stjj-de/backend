package de.stjj.backend.utils.json

import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.time.Instant

val moshi: Moshi = Moshi.Builder()
        .add(Instant::class.java, object : JsonAdapter<Instant>() {
            override fun fromJson(reader: JsonReader): Instant? {
                try {
                    return try {
                        reader.nextNull<Any>()
                        null
                    } catch (e: Exception) {
                        Instant.parse(reader.nextString())
                    }
                } catch (e: Exception) {
                    throw JsonDataException("Date string required")
                }
            }

            override fun toJson(writer: JsonWriter, value: Instant?) {
                writer.value(value?.toString())
            }
        })
        .add(KotlinJsonAdapterFactory())
        .build()

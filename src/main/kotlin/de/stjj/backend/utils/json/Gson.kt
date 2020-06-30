package de.stjj.backend.utils.json

import com.google.gson.*
import java.lang.reflect.Type
import java.time.LocalDateTime

val gson = GsonBuilder()
        .serializeNulls()
        .disableHtmlEscaping()
        .registerTypeAdapter(LocalDateTime::class.java, object : JsonSerializer<LocalDateTime> {
            override fun serialize(src: LocalDateTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
                return JsonPrimitive(src?.toString())
            }
        })
        .create()!!

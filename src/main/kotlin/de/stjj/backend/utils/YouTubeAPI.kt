package de.stjj.backend.utils

import com.google.gson.JsonObject
import de.stjj.backend.utils.json.gson
import okhttp3.OkHttpClient
import okhttp3.Request

object YouTubeAPI {
    private val httpClient = OkHttpClient()

    fun getVideoTitle(id: String): String? {
        val key = System.getenv("YOUTUBE_API_KEY")

        val response = httpClient.newCall(
            Request.Builder()
                .url("https://www.googleapis.com/youtube/v3/videos?part=snippet&id=$id&key=$key")
                .build()
        ).execute()

        return if (response.code == 404) null else {
            val body = response.body!!

            val data = gson.fromJson(body.string(), JsonObject::class.java)
            body.close()

            data.get("snippet")?.asJsonObject?.get("title")?.asString
        }
    }
}

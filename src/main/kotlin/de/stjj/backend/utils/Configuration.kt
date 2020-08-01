package de.stjj.backend.utils

val isDev: Boolean = System.getenv("ENV").equals("development", true)
val tzOffsetHours = System.getenv("TZ_OFFSET_HOURS").toInt()
val hostname: String = System.getenv("HOSTNAME")
val youtubeAPIKey: String = System.getenv("YOUTUBE_API_KEY")
val mariadbHost = System.getenv("MARIADB_HOST") ?: "127.0.0.1"
val mariadbPort = System.getenv("MARIADB_PORT") ?: "3306"
val mariadbDatabase = System.getenv("MARIADB_DATABASE") ?: "stjj-de"
val mariadbUser = System.getenv("MARIADB_USER") ?: "stjj"
val mariadbPassword = System.getenv("MARIADB_PASSWORD")
        ?: error("Please specify the MARIADB_PASSWORD environment variable")

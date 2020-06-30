package de.stjj.backend.utils

val isDev: Boolean = System.getenv("ENV").equals("development", true)
val hostname: String = System.getenv("HOSTNAME")

package de.stjj.backend.utils

import java.net.URLEncoder

// Like in JavaScript
fun encodeURIComponent(string: String) = URLEncoder.encode(string, Charsets.UTF_8.toString()).replace("+", "%20")

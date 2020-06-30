@file:Suppress("EXPERIMENTAL_API_USAGE")

package de.stjj.backend.routes

import io.jooby.Kooby

fun Kooby.filesRoutes() {
    get("/files/profile-images/{id}") {
        TODO("Respond with a profile image")
    }

    get("/files/uploads/{id}/{filename}") {
        TODO("Redirect or respond with an uploaded file")
    }

    get("/f/{id}") {
        TODO("Redirect to the actual file of the alias")
    }
}

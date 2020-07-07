package de.stjj.backend.routes.api

import de.stjj.backend.models.Posts
import de.stjj.backend.utils.apiModelRoutes
import io.jooby.Kooby

@ExperimentalStdlibApi
fun Kooby.postsRoutes() {
    apiModelRoutes("/posts", Posts)
}

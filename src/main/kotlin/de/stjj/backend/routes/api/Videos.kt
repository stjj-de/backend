package de.stjj.backend.routes.api

import de.stjj.backend.models.Videos
import de.stjj.backend.utils.apiModelRoutes
import io.jooby.Kooby

@ExperimentalStdlibApi
fun Kooby.videosRoutes() {
    apiModelRoutes("/videos", Videos)
}

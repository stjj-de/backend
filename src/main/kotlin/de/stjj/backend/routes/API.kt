package de.stjj.backend.routes

import de.stjj.backend.routes.api.*
import io.jooby.Kooby

@ExperimentalStdlibApi
fun Kooby.apiRoutes() {
    path("/api") {
        authRoutes()
        churchServiceDatesRoutes()
        churchesRoutes()
        eventsRoutes()
        postsRoutes()
        uploadsRoutes()
        usersRoutes()
        videosRoutes()
    }
}

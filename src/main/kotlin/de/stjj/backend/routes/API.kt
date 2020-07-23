package de.stjj.backend.routes

import de.stjj.backend.models.*
import de.stjj.backend.routes.api.authRoutes
import de.stjj.backend.routes.api.contentsRoutes
import de.stjj.backend.routes.api.usersRoutes
import de.stjj.backend.utils.apiModelRoutes
import io.jooby.Kooby

@ExperimentalStdlibApi
fun Kooby.apiRoutes() {
    path("/api") {
        // TODO: Add caching where appropriate

        apiModelRoutes("/events", Events)
        apiModelRoutes("/church-service-dates", ChurchServiceDates)
        apiModelRoutes("/churches", Churches)
        apiModelRoutes("/posts", Posts)
        apiModelRoutes("/uploads", UploadedFiles)
        apiModelRoutes("/videos", Videos)
        apiModelRoutes("/groups", Groups)

        usersRoutes()
        authRoutes()
        contentsRoutes()
    }
}

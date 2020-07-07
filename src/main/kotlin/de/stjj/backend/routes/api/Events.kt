package de.stjj.backend.routes.api

import de.stjj.backend.models.Events
import de.stjj.backend.utils.apiModelRoutes
import io.jooby.Kooby

@ExperimentalStdlibApi
fun Kooby.eventsRoutes() {
    apiModelRoutes("/events", Events)
}

package de.stjj.backend.routes.api

import de.stjj.backend.models.Churches
import de.stjj.backend.utils.apiModelRoutes
import io.jooby.Kooby

@ExperimentalStdlibApi
fun Kooby.churchesRoutes() {
    apiModelRoutes("/churches", Churches)
}

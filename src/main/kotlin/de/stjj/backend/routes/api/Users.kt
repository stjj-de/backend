package de.stjj.backend.routes.api

import de.stjj.backend.models.Users
import de.stjj.backend.utils.apiModelRoutes
import io.jooby.Kooby

@ExperimentalStdlibApi
fun Kooby.usersRoutes() {
    apiModelRoutes("/users", Users)
}

package de.stjj.backend.routes.api

import de.stjj.backend.models.ChurchServiceDates
import de.stjj.backend.utils.apiModelRoutes
import io.jooby.Kooby

@ExperimentalStdlibApi
fun Kooby.churchServiceDatesRoutes() {
    apiModelRoutes("/church-service-dates", ChurchServiceDates)
}

package de.stjj.backend.routes.api

import de.stjj.backend.models.UploadedFiles
import de.stjj.backend.utils.apiModelRoutes
import io.jooby.Kooby

@ExperimentalStdlibApi
fun Kooby.uploadsRoutes() {
    apiModelRoutes("/uploads", UploadedFiles)
}

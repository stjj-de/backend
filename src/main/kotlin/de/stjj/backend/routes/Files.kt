package de.stjj.backend.routes

import de.stjj.backend.models.UploadedFile
import de.stjj.backend.utils.encodeURIComponent
import io.jooby.Kooby
import io.jooby.StatusCode
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.nio.file.Paths

private const val DATA_DIR_ENV_VAR = "DATA_DIR"

fun getFileForUploadedFile(id: String): File = Paths.get(System.getenv(DATA_DIR_ENV_VAR), "uploads", id).toFile()

fun Kooby.filesRoutes() {
    if (System.getenv(DATA_DIR_ENV_VAR) == null) throw Error("DATA_DIR env variable not specified")

    get("/files/{id}") {
        val uploadedFile = transaction { UploadedFile.findById(ctx.path("id").value()) }
        if (uploadedFile == null) ctx.send(StatusCode.NOT_FOUND)
        else ctx.sendRedirect("/files/${uploadedFile.id}/${encodeURIComponent(uploadedFile.title)}")
    }

    get("/files/{id}/{title}") {
        val uploadedFile = transaction { UploadedFile.findById(ctx.path("id").value()) }
        if (uploadedFile == null) ctx.send(StatusCode.NOT_FOUND)
        else {
            val path = "/files/${uploadedFile.id}/${encodeURIComponent(uploadedFile.title)}"

            if (ctx.requestPath != path) ctx.sendRedirect(path)
            else ctx.send(getFileForUploadedFile(uploadedFile.id.value).inputStream())
        }
    }
}

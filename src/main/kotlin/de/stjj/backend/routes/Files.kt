package de.stjj.backend.routes

import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import de.stjj.backend.models.UploadedFile
import de.stjj.backend.models.User
import de.stjj.backend.models.hasHigherOrEqualRole
import de.stjj.backend.utils.*
import io.jooby.Kooby
import io.jooby.StatusCode
import org.apache.tika.Tika
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Files
import java.time.LocalDateTime

fun getPathForUploadedFile(uploadedFile: UploadedFile) = "/files/${uploadedFile.id}/${encodeURIComponent(uploadedFile.title)}${uploadedFile.mimeType?.extension ?: ""}"

fun Kooby.filesRoutes() {
    if (System.getenv(DATA_DIR_ENV_VAR) == null) throw Error("DATA_DIR env variable not specified")

    get("/files/{id}") {
        val uploadedFile = transaction { UploadedFile.findById(ctx.path("id").value()) }
        if (uploadedFile == null) ctx.send(StatusCode.NOT_FOUND)
        else ctx.sendRedirect(getPathForUploadedFile(uploadedFile))
    }

    post("/files") {
        if (!ctx.user.hasHigherOrEqualRole(User.Role.NONE)) {
            ctx.send(StatusCode.FORBIDDEN)
        } else {
            val id = NanoIdUtils.randomNanoId().take(10)
            val file = ctx.file("file")
            val mimeType: String? = Tika().detect(file.path())

            val uploadedFile = transaction {
                UploadedFile.new(id) {
                    title = file.fileName.toByteArray().take(255).toByteArray().toString(Charsets.UTF_8)
                    uploader = ctx.userEntityID
                    uploadedAt = LocalDateTime.now()
                    mimeTypeName = mimeType
                }
            }

            Files.move(file.path(), getFileForUploadedFile(uploadedFile).toPath())
            ctx.setResponseHeader("Location", "/files/${id}")
            ctx.send(StatusCode.CREATED)
        }
    }

    get("/files/{id}/{title}") {
        val uploadedFile = transaction { UploadedFile.findById(ctx.path("id").value()) }
        if (uploadedFile == null) ctx.send(StatusCode.NOT_FOUND)
        else {
            val path = getPathForUploadedFile(uploadedFile)

            if (ctx.requestPath != path) ctx.sendRedirect(path)
            else {
                ctx.setResponseType(uploadedFile.mimeTypeName ?: "application/octet-stream")
                ctx.send(getFileForUploadedFile(uploadedFile).inputStream())
            }
        }
    }
}

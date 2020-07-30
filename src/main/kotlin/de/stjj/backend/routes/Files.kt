package de.stjj.backend.routes

import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import de.stjj.backend.models.UploadedFile
import de.stjj.backend.models.User
import de.stjj.backend.models.hasHigherOrEqualRole
import de.stjj.backend.utils.*
import io.jooby.Kooby
import io.jooby.StatusCode
import org.apache.tika.Tika
import org.apache.tika.mime.MimeType
import org.apache.tika.mime.MimeTypes
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Files
import java.time.LocalDateTime

fun getPathForUploadedFile(uploadedFile: UploadedFile) = "/files/${uploadedFile.id}/${encodeURIComponent(uploadedFile.title)}${uploadedFile.mimeType?.extension ?: ""}"

private val tika = Tika()

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
            val mimeType: MimeType? = tika.detect(file.path())?.let { MimeTypes.getDefaultMimeTypes().forName(it) }
            var fileName = file.fileName
            if (mimeType != null) {
                val actualExtension = "." + fileName.split(".").last()
                val correctExtensions = mimeType.extensions

                if (correctExtensions.contains(actualExtension)) {
                    fileName = fileName.removeSuffix(actualExtension)
                }
            }

            val uploadedFile = transaction {
                UploadedFile.new(id) {
                    title = fileName.toByteArray().take(255).toByteArray().toString(Charsets.UTF_8)
                    uploader = ctx.userEntityID
                    uploadedAt = LocalDateTime.now()
                    mimeTypeName = mimeType?.name
                }
            }

            Files.move(file.path(), getFileForUploadedFile(uploadedFile).toPath())
            ctx.responseCode = StatusCode.CREATED
            ctx.setResponseHeader("Location", "/files/${id}")
            ctx.send(id)
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

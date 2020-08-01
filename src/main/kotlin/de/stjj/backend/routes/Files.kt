package de.stjj.backend.routes

import de.stjj.backend.models.UploadedFile
import de.stjj.backend.models.UploadedFiles
import de.stjj.backend.models.User
import de.stjj.backend.models.hasHigherOrEqualRole
import de.stjj.backend.routes.api.APIException
import de.stjj.backend.utils.*
import io.jooby.HandlerContext
import io.jooby.Kooby
import io.jooby.StatusCode
import org.apache.tika.Tika
import org.apache.tika.mime.MimeType
import org.apache.tika.mime.MimeTypes
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Files
import java.time.LocalDateTime

fun getURLPathForUploadedFile(uploadedFile: UploadedFile): String {
    val builder = StringBuilder()
    builder.append("/files/${uploadedFile.id}")

    val title = uploadedFile.title
    if (uploadedFile.mimeTypeName == "application/pdf" && title != null) {
        builder.append("/${encodeURIComponent(title)}${uploadedFile.mimeType?.extension ?: ""}")
    }

    return builder.toString()
}

private val tika = Tika()

fun Kooby.filesRoutes() {
    post("/files") {
        if (!ctx.user.hasHigherOrEqualRole(User.Role.NONE)) {
            ctx.send(StatusCode.FORBIDDEN)
        } else {
            val requiredMimeType = ctx.query("requiredMimeType").valueOrNull()
            val file = ctx.file("file")
            val hash = getSha256OfFile(file.path().toFile())

            val fileAlreadyUploaded = transaction {
                UploadedFiles
                    .slice(UploadedFiles.id)
                    .select(with (SqlExpressionBuilder) { UploadedFiles.id eq hash })
                    .limit(1)
                    .firstOrNull()
            } != null

            if (!fileAlreadyUploaded || requiredMimeType != null) {
                val mimeType: MimeType? = tika.detect(file.path())?.let { MimeTypes.getDefaultMimeTypes().forName(it) }
                val actualMimeType = mimeType?.name ?: "application/octet-stream"

                if (requiredMimeType != null && requiredMimeType != actualMimeType) {
                    throw APIException(
                        StatusCode.UNSUPPORTED_MEDIA_TYPE,
                        "MIME_TYPE_NOT_EXPECTED",
                        "The actual mime type of the uploaded file does not match the one specified in the request.",
                        details = mapOf(
                            "required" to requiredMimeType,
                            "actual" to actualMimeType
                        )
                    )
                }

                if (!fileAlreadyUploaded) {
                    var fileName = file.fileName
                    if (mimeType != null) {
                        val actualExtension = "." + fileName.split(".").last()
                        val correctExtensions = mimeType.extensions

                        if (correctExtensions.contains(actualExtension)) {
                            fileName = fileName.removeSuffix(actualExtension)
                        }
                    }

                    val uploadedFile = transaction {
                        UploadedFile.new(hash) {
                            title = fileName.toByteArray().take(255).toByteArray().toString(Charsets.UTF_8)
                            firstUploader = ctx.userEntityID
                            uploadedAt = LocalDateTime.now()
                            mimeTypeName = mimeType?.name
                        }
                    }

                    Files.move(file.path(), getFileForUploadedFile(uploadedFile).toPath())
                    ctx.responseCode = StatusCode.CREATED
                }
            }

            if (fileAlreadyUploaded) ctx.responseCode = StatusCode.OK

            ctx.send(hash)
        }
    }

    get("/files/{id}") { handleGetFile() }
    get("/files/{id}/*") { handleGetFile() }
}

fun HandlerContext.handleGetFile() {
    val uploadedFile = transaction { UploadedFile.findById(ctx.path("id").value()) }
    if (uploadedFile == null) ctx.send(StatusCode.NOT_FOUND)
    else {
        val path = getURLPathForUploadedFile(uploadedFile)

        if (ctx.requestPath != path) ctx.sendRedirect(path)
        else {
            ctx.setResponseType(uploadedFile.mimeTypeName ?: "application/octet-stream")
            ctx.setResponseHeader("Cache-Control", "immutable, public, max-age=31557600") // Cache for one year
            ctx.send(getFileForUploadedFile(uploadedFile).inputStream())
        }
    }
}

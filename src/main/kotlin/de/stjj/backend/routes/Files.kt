package de.stjj.backend.routes

import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import com.google.common.base.CaseFormat
import de.stjj.backend.models.*
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
import java.io.File
import java.io.FileNotFoundException
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
    Files.createDirectories(File(dataDir).resolve("tmp").toPath())
    Files.createDirectories(File(dataDir).resolve("uploads").toPath())

    post("/files") {
        if (!ctx.user.hasHigherOrEqualRole(User.Role.NONE)) {
            ctx.send(StatusCode.FORBIDDEN)
        } else {
            val allowedMimeTypes = ctx.query("allowedMimeTypes").valueOrNull()?.split(";")
            val file = try {
                ctx.file("file")
            } catch (e: FileNotFoundException) {
                throw APIException(StatusCode.BAD_REQUEST, "NO_FILE", "No file in request.")
            }

            val tempPath = File(dataDir).resolve("tmp/${NanoIdUtils.randomNanoId()}").toPath()
            Files.move(file.path(), tempPath)

            val hash = getSha256OfFile(tempPath.toFile())

            val fileAlreadyUploaded = transaction {
                UploadedFiles
                    .slice(UploadedFiles.id)
                    .select(with (SqlExpressionBuilder) { UploadedFiles.id eq hash })
                    .limit(1)
                    .firstOrNull()
            } != null

            var mimeType: MimeType? = null

            if (!fileAlreadyUploaded || allowedMimeTypes != null) {
                mimeType = tika.detect(tempPath)?.let { MimeTypes.getDefaultMimeTypes().forName(it) }
                val actualMimeType = mimeType?.name ?: "application/octet-stream"

                if (allowedMimeTypes != null && !allowedMimeTypes.contains(actualMimeType)) {
                    throw APIException(
                        StatusCode.UNSUPPORTED_MEDIA_TYPE,
                        "MIME_TYPE_NOT_ALLOWED",
                        "The mime type of the uploaded file is not allowed.",
                        details = mapOf(
                            "allowed" to allowedMimeTypes,
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

                    Files.move(tempPath, getFileForUploadedFile(uploadedFile).toPath())
                    ctx.responseCode = StatusCode.CREATED
                }
            }

            Files.deleteIfExists(tempPath)

            if (fileAlreadyUploaded) ctx.responseCode = StatusCode.OK

            mapOf(
                "id" to hash,
                "mimeType" to mimeType?.name,
                "isNew" to !fileAlreadyUploaded
            )
        }
    }

    get("/files/from-content/{id}") {
        val idString = ctx.path("id").value()

        var invalid = true

        val id = runCatching {
            Content.ID.valueOf(CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_UNDERSCORE, idString))
        }.getOrNull()

        if (id != null) {
            if (id.file) {
                invalid = false

                val content = transaction { Content.findById(id) }
                if (content != null) {
                    ctx.sendRedirect("/files/${content.content}")
                    return@get Unit
                }
            }
        }

        ctx.sendRedirect("/file404?invalid=$invalid&content=$idString")
    }

    get("/files/{id}") { handleGetFile() }
    get("/files/{id}/*") { handleGetFile() }
}

fun HandlerContext.handleGetFile() {
    val id = ctx.path("id").value()
    val uploadedFile = transaction { UploadedFile.findById(id) }
    if (uploadedFile == null) ctx.sendRedirect("/file404?id=$id")
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

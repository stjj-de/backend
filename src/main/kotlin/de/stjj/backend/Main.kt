package de.stjj.backend

import com.squareup.moshi.JsonDataException
import de.stjj.backend.models.*
import de.stjj.backend.routes.api.APIErrorResponse
import de.stjj.backend.routes.api.APIException
import de.stjj.backend.routes.api.towerPictureRoutes
import de.stjj.backend.routes.apiRoutes
import de.stjj.backend.routes.filesRoutes
import de.stjj.backend.utils.*
import de.stjj.backend.utils.json.gson
import de.stjj.backend.utils.json.moshi
import io.jooby.MediaType
import io.jooby.RouterOption
import io.jooby.StatusCode
import io.jooby.exception.MethodNotAllowedException
import io.jooby.exception.NotFoundException
import io.jooby.exception.TypeMismatchException
import io.jooby.runApp
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun main(args: Array<String>) {
    connectToDatabase()

    runApp(args) {
        encoder { _, value -> gson.toJson(value).toByteArray() }
        decoder(MediaType.json) { ctx, type -> moshi.adapter<Any>(type).failOnUnknown().fromJson(ctx.body().value())!! }

        before { ctx -> ctx.setResponseHeader("Server", "STJJ Backend") }
        enableAuth()

        filesRoutes()
        apiRoutes()
        towerPictureRoutes()

        error(JsonDataException::class.java) { ctx, e, _ ->
            ctx.responseCode = StatusCode.BAD_REQUEST
            ctx.render(APIErrorResponse(
                    "INVALID_REQUEST_DATA",
                    "The provided request data is invalid.",
                    mapOf(
                            "reason" to (e as JsonDataException).message
                    )
            ))
        }

        APIException.registerHandler(this)

        error(TypeMismatchException::class.java) { ctx, e, _ ->
            e as TypeMismatchException
            ctx.responseCode = StatusCode.BAD_REQUEST
            ctx.render(APIErrorResponse(
                    "INVALID_REQUEST_PARAM",
                    "The request parameter named '${e.name}' has the wrong type.",
                    mapOf("paramName" to e.name)
            ))
        }

        error(NotFoundException::class.java) { ctx, _, _ ->
            ctx.responseCode = StatusCode.NOT_FOUND
            ctx.send("")
        }

        error(MethodNotAllowedException::class.java) { ctx, _, _ ->
            ctx.responseCode = StatusCode.METHOD_NOT_ALLOWED
            ctx.send("")
        }

        error(Exception::class.java) { ctx, e, _ ->
            ctx.responseCode = StatusCode.SERVER_ERROR
            ctx.render(APIErrorResponse(
                    "INTERNAL",
                    "An internal server error occurred.",
                    if (isDev) mapOf("message" to e.message) else null
            ))

            e.printStackTrace()
        }

        serverOptions {
            port = 8000
        }

        routerOptions(RouterOption.IGNORE_TRAILING_SLASH)
    }
}

fun connectToDatabase() {
    Database.connect(
            "jdbc:mysql://$mariadbHost:$mariadbPort/$mariadbDatabase",
            user = mariadbUser,
            password = mariadbPassword
    )

    transaction {
        SchemaUtils.createMissingTablesAndColumns(
                Churches,
                ChurchServiceDates,
                Users,
                UploadedFiles,
                Contents,
                Videos,
                Posts,
                Events,
                Groups,
                GroupMembers
        )
    }
}

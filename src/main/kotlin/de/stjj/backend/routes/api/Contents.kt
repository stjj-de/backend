package de.stjj.backend.routes.api

import de.stjj.backend.models.Content
import io.jooby.Kooby
import io.jooby.MediaType
import io.jooby.StatusCode
import org.jetbrains.exposed.sql.transactions.transaction

@ExperimentalStdlibApi
fun Kooby.contentsRoutes() {
    path("/contents") {
        // TODO: Allow updates

        get("/{id}") {
            val idName = ctx.path("id").value()
            val id = kotlin.runCatching { Content.ID.valueOf(idName) }.getOrNull()
            val content = id?.let { transaction { Content.findById(it) } }

            val data = if (content == null) {
                ctx.responseCode = StatusCode.NOT_FOUND
                null
            } else content.content

            if (ctx.accept(MediaType.json) && !ctx.accept(MediaType.text)) {
                mapOf("data" to data)
            } else {
                data ?: ""
            }
        }
    }
}

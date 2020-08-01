package de.stjj.backend.routes.api

import de.stjj.backend.models.Content
import de.stjj.backend.models.User
import de.stjj.backend.models.isHigherOrEqual
import de.stjj.backend.utils.APIModel
import de.stjj.backend.utils.InsufficientPermissionsException
import de.stjj.backend.utils.user
import io.jooby.Kooby
import io.jooby.MediaType
import io.jooby.StatusCode
import org.jetbrains.exposed.sql.transactions.transaction

fun Kooby.contentsRoutes() {
    path("/contents") {
        put("/{id}") {
            if (ctx.user?.role?.isHigherOrEqual(User.Role.EDITOR) != true)
                throw InsufficientPermissionsException("You are not allowed to modify contents.")

            val id: Content.ID

            try {
                id = Content.ID.valueOf(ctx.path("id").value())
            } catch (e: IllegalArgumentException) {
                throw APIModel.InvalidResourceIDException("There is no content with this ID.")
            }

            transaction {
                val entity = Content.findById(id)
                val text = ctx.body().value(Charsets.UTF_8)

                if (entity == null) {
                    Content.new(id) {
                        content = text
                    }
                } else {
                    entity.content = text
                }
            }
        }

        get("/{id}") {
            val jsonResponse = ctx.accept(MediaType.json) && !ctx.accept(MediaType.text)
            val id: Content.ID

            try {
                id = Content.ID.valueOf(ctx.path("id").value())
            } catch (e: IllegalArgumentException) {
                ctx.responseCode = StatusCode.NOT_FOUND
                return@get if (jsonResponse) mapOf("data" to null) else ""
            }

            val content = transaction { Content.findById(id) }

            val data = content?.content ?: ""

            if (jsonResponse) mapOf("data" to data) else data
        }
    }
}

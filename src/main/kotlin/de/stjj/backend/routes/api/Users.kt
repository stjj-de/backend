package de.stjj.backend.routes.api

import de.stjj.backend.models.Users
import de.stjj.backend.utils.apiModelRoutes
import de.stjj.backend.utils.getUserImageFile
import io.jooby.Kooby
import io.jooby.StatusCode
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

@ExperimentalStdlibApi
fun Kooby.usersRoutes() {
    apiModelRoutes("/users", Users)

    get("/users/{id}/image") {
        val user = transaction {
            Users
                    .slice(Users.id)
                    .select { Users.getIDGetOneSelectExpression(ctx.path("id")) }
                    .limit(1)
                    .firstOrNull()
        }

        if (user == null) ctx.send(StatusCode.NOT_FOUND)
        else ctx.send(getUserImageFile(user[Users.id].value).inputStream())
    }
}

package de.stjj.backend.utils

import de.stjj.backend.models.User
import de.stjj.backend.models.Users
import de.stjj.backend.routes.api.APIException
import io.jooby.Context
import io.jooby.Cookie
import io.jooby.Kooby
import io.jooby.StatusCode
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class InvalidAuthenticationTokenException: APIException(
        StatusCode.UNAUTHORIZED,
        "INVALID_AUTHENTICATION_TOKEN",
        "The authentication token you provided is invalid.",
        { it.setResponseCookie(Cookie("token").setMaxAge(0)) }
)

class AuthenticationRequiredException: APIException(
        StatusCode.FORBIDDEN,
        "AUTHENTICATION_REQUIRED",
        "You must be authenticated to use this endpoint."
)

open class InsufficientPermissionsException(message: String = "You are not allowed to do this.", details: Map<String, Any?>? = null): APIException(
        StatusCode.FORBIDDEN,
        "INSUFFICIENT_PERMISSIONS",
        message,
        details = details
)

@Suppress("UNCHECKED_CAST")
val Context.userEntityID get() = attributes["userEntityID"] as EntityID<Int>?
val Context.userID get() = userEntityID?.value
val Context.user get() = attributes.getOrPut("user") {
    userEntityID?.let { id -> transaction { User.findById(id) } }
} as User?

fun Kooby.enableAuth() {
    before { ctx ->
        val token = ctx.cookie("token").valueOrNull()
        if (token.isNullOrBlank()) return@before

        val id = transaction {
            Users
                    .slice(Users.id)
                    .select { Users.authToken eq token }
                    .limit(1)
                    .firstOrNull()
                    ?.get(Users.id)
        } ?: throw InvalidAuthenticationTokenException()

        ctx.attribute("userEntityID", id)
    }
}

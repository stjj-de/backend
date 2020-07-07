package de.stjj.backend.utils

import de.stjj.backend.models.User
import de.stjj.backend.models.Users
import de.stjj.backend.routes.api.APIException
import io.jooby.Context
import io.jooby.Kooby
import io.jooby.StatusCode
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class InvalidAuthenticationTokenException: APIException(
        StatusCode.UNAUTHORIZED,
        "INVALID_AUTHENTICATION_TOKEN",
        "The authentication token you provided is invalid."
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
        details
)

val Context.userID get() = attributes["userID"] as Int?
val Context.user get() = attributes.getOrPut("user") {
    userID?.let { id -> transaction { User.findById(id) } }
} as User?

fun Kooby.enableAuth() {
    before { ctx ->
        val token = ctx.cookie("token").valueOrNull()
        if (token.isNullOrBlank()) return@before

        val userID = transaction {
            Users
                    .slice(Users.id)
                    .select { Users.authToken eq token }
                    .limit(1)
                    .firstOrNull()
                    ?.get(Users.id)?.value
        } ?: throw InvalidAuthenticationTokenException()

        ctx.attribute("userID", userID)
    }
}

package de.stjj.backend.routes.api

import at.favre.lib.crypto.bcrypt.BCrypt
import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import de.stjj.backend.models.User
import de.stjj.backend.utils.AuthenticationRequiredException
import de.stjj.backend.utils.hostname
import de.stjj.backend.utils.isDev
import de.stjj.backend.utils.userID
import io.jooby.Kooby
import io.jooby.StatusCode
import org.jetbrains.exposed.sql.transactions.transaction
import java.security.SecureRandom
import java.util.concurrent.TimeUnit

private val bcryptVerifier = BCrypt.verifyer()
private val tokenAlphabet = "0123456789ABCDEFGHNRVfgctiUvz_KqYTJkLxpZXIjQW".toCharArray()

val tokenCookieMaxAge = TimeUnit.SECONDS.convert(30, TimeUnit.DAYS)

data class LoginBody(val id: Int, val password: String)

fun Kooby.authRoutes() {
    path("auth") {
        post("/") { ctx ->
            // Login
            val body = ctx.body(LoginBody::class.java)
            val user = transaction { User.findById(body.id) }

            val password = body.password.toByteArray().take(71).toByteArray()

//            println(BCrypt.withDefaults().hash(12, password).decodeToString())

            if (user != null) {
                val result = runCatching { bcryptVerifier.verify(password, user.passwordHash.toByteArray()) }.getOrNull()
                if (result?.verified == true) {
                    val token = NanoIdUtils.randomNanoId(SecureRandom(), tokenAlphabet, 50)
                    transaction { user.authToken = token }

                    ctx.responseCode = StatusCode.NO_CONTENT
                    ctx.setResponseHeader(
                            "Set-Cookie",
                            "token=${token}; HttpOnly; Path=/; SameSite=Strict; " +
                                    "Max-Age=$tokenCookieMaxAge; " +
                                    if (isDev) "" else "Domain: $hostname; Secure"
                    )

                    return@post ""
                }
            }

            throw AuthenticationFailedException()
        }

        delete("/") {
            ctx.userID ?: throw AuthenticationRequiredException()

            ctx.setResponseHeader(
                    "Set-Cookie",
                    "token=; HttpOnly; Path=/; SameSite=Strict; " +
                            "Max-Age=0" +
                            if (isDev) "" else "Domain: $hostname; Secure"
            )

            ctx.send(StatusCode.NO_CONTENT)
        }

        get("/me") { ctx ->
            val userID = ctx.userID ?: throw AuthenticationRequiredException()
            mapOf("id" to userID)
        }
    }
}

class AuthenticationFailedException: APIException(
        StatusCode.UNAUTHORIZED,
        "AUTHENTICATION_FAILED",
        "The authentication failed."
)

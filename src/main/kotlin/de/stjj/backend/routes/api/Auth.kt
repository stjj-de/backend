package de.stjj.backend.routes.api

import at.favre.lib.crypto.bcrypt.BCrypt
import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import de.stjj.backend.models.User
import de.stjj.backend.utils.*
import io.jooby.Kooby
import io.jooby.StatusCode
import org.jetbrains.exposed.sql.transactions.transaction
import java.security.SecureRandom
import java.util.concurrent.TimeUnit

private val bcryptVerifier = BCrypt.verifyer()
private val tokenAlphabet = "0123456789ABCDEFGHNRVfgctiUvz_KqYTJkLxpZXIjQW".toCharArray()
private val tokenCookieMaxAge = TimeUnit.SECONDS.convert(30, TimeUnit.DAYS)

data class LoginBody(val id: Int, val password: String)

@OptIn(ExperimentalStdlibApi::class)
fun Kooby.authRoutes() {
    path("auth") {
        post("/") { ctx ->
            // Login
            val body = ctx.body(LoginBody::class.java)
            val user = transaction { User.findById(body.id) }

            val password = body.password.take(71).toByteArray()

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
            ctx.user ?: throw AuthenticationRequiredException()
            transaction { ctx.user!!.authToken = null }

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

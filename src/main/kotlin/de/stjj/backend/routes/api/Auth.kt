package de.stjj.backend.routes.api

import at.favre.lib.crypto.bcrypt.BCrypt
import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import de.stjj.backend.models.User
import de.stjj.backend.utils.AuthenticationRequiredException
import de.stjj.backend.utils.hostname
import de.stjj.backend.utils.isDev
import de.stjj.backend.utils.user
import io.jooby.Kooby
import io.jooby.StatusCode
import org.jetbrains.exposed.sql.transactions.transaction
import java.security.SecureRandom
import java.util.concurrent.TimeUnit

private val bcryptVerifier = BCrypt.verifyer()
private val tokenAlphabet = "0123456789ABCDEFGHNRVfgctiUvz_KqYTJkLxpZXIjQW".toCharArray()

val tokenCookieMaxAge = TimeUnit.SECONDS.convert(30, TimeUnit.DAYS)

data class LoginBody(val id: Int, val password: String)

@ExperimentalStdlibApi
fun Kooby.authRoutes() {
    path("auth") {
        post("/") { ctx ->
            // Login
            val body = ctx.body(LoginBody::class.java)
            val user = transaction { User.findById(body.id) }

//            println(BCrypt.withDefaults().hash(12, body.password.toCharArray()).decodeToString())

            if (user != null) {
                val result = runCatching { bcryptVerifier.verify(body.password.toCharArray(), user.passwordHash) }.getOrNull()
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
            // TODO: Logout
        }

        get("/me") { ctx ->
            val user = ctx.user
            if (user == null) throw AuthenticationRequiredException()
            else mapOf("id" to user.id.value)
        }
    }
}

class AuthenticationFailedException: APIException(
        StatusCode.UNAUTHORIZED,
        "AUTHENTICATION_FAILED",
        "The authentication failed."
)

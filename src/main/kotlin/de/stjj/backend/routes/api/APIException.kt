package de.stjj.backend.routes.api

import io.jooby.Context
import io.jooby.Kooby
import io.jooby.StatusCode

data class APIErrorResponse(val code: String, val message: String, val details: Any?)

open class APIException(
    val statusCode: StatusCode,
    val code: String,
    override val message: String,
    val details: Any? = null,
    val manipulateResponse: (ctx: Context) -> Unit = {}
): RuntimeException() {
    companion object {
        fun registerHandler(app: Kooby) {
            app.error(APIException::class.java) { ctx, e, _ ->
                e as APIException
                ctx.responseCode = e.statusCode
                e.manipulateResponse(ctx)
                ctx.render(APIErrorResponse(e.code, e.message, e.details))
            }
        }
    }
}

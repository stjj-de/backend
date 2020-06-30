package de.stjj.backend.utils

import de.stjj.backend.routes.api.APIException
import io.jooby.Context
import io.jooby.StatusCode

class InvalidPaginationOptionException(message: String): APIException(
        StatusCode.BAD_REQUEST,
        "INVALID_PAGINATION_OPTION",
        message
)

val Context.limitAndOffset: Pair<Int, Int> get() {
    val limit = query("limit").intValue(10)
    val offset = query("offset").intValue(0)
    if (limit < 1 || limit > 50) throw InvalidPaginationOptionException("The limit must be between 1 and 50")
    if (offset < 0) throw InvalidPaginationOptionException("The offset must be positive.")
    return Pair(limit, offset)
}

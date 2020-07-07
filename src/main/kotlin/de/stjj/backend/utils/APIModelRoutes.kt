package de.stjj.backend.utils

import de.stjj.backend.models.User
import de.stjj.backend.routes.api.APIException
import io.jooby.Context
import io.jooby.Kooby
import io.jooby.StatusCode
import io.jooby.Value
import org.jetbrains.exposed.dao.DaoEntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

interface APIModel {
    val writeAllowedRole: User.Role
    val defaultFields: String
    val apiFields: Set<APIField>
    val getOneSelectExpression: (idValue: Value) -> Op<Boolean>
    val getAllSelectExpression: ((ctx: Context) -> Op<Boolean>?)? get() = null

    class InvalidResourceIDException(
            message: String = "The specified resource ID is invalid."
    ): APIException(StatusCode.BAD_REQUEST, "INVALID_RESOURCE_ID", message)

    companion object {
        fun createIntIDGetOneSelectExpression(table: IntIdTable): (Value) -> Op<Boolean> {
            return { idValue: Value ->
                with(SqlExpressionBuilder) {
                    val id = idValue.intValue()
                    if (id < 0) throw InvalidResourceIDException("The resource ID must be positive.")
                    table.id eq id
                }
            }
        }
    }
}

sealed class APIField(val name: String, val role: User.Role) {
    class C(name: String, val column: Column<*>, role: User.Role = User.Role.NONE): APIField(name, role)
    class G(name: String, val dependsOn: Set<Column<*>> = emptySet(), role: User.Role = User.Role.NONE, val getter: (ResultRow) -> Any?): APIField(name, role)
}

class UnknownFieldException(fieldName: String): APIException(
        StatusCode.BAD_REQUEST,
        "UNKNOWN_FIELD",
        "There is no field named $fieldName on this model.",
        mapOf("fieldName" to fieldName)
)

class FieldAccessNotAllowedException(fieldName: String): InsufficientPermissionsException(
        "You are not allowed to access the field named $fieldName on this model.",
        mapOf("fieldName" to fieldName)
)

private data class ParseResult(
        val columns: Set<Column<*>>,
        val resultRowMapper: (row: ResultRow) -> Map<String, *>
)

@ExperimentalStdlibApi
fun Kooby.apiModelRoutes(pattern: String, model: APIModel) {
    if (model !is Table) throw Error("${model::class.simpleName} is not a Table")

    fun parseFields(ctx: Context): ParseResult {
        val fieldNames = ctx.query("fields").value(model.defaultFields).split(",")
        val requestedFields = fieldNames.map { name -> model.apiFields.find { it.name == name } ?: throw UnknownFieldException(name) }
        for (field in requestedFields) {
            val userRoleLevel = ctx.user?.role?.ordinal ?: 0
            val requiredLevel = field.role.ordinal
            if (userRoleLevel < requiredLevel) throw FieldAccessNotAllowedException(field.name)
        }

        val requestedColumnFields = requestedFields.filterIsInstance<APIField.C>()
        val requestedGetterFields = requestedFields.filterIsInstance<APIField.G>()

        val columns = requestedColumnFields.map(APIField.C::column).toMutableSet()
        requestedGetterFields.forEach { columns.addAll(it.dependsOn) }

        return ParseResult(columns) { row ->
            val data = mutableMapOf<String, Any?>()

            for (field in requestedFields) {
                val value =
                        if (field is APIField.C) {
                            val v = row[field.column]
                            if (v is DaoEntityID<*>) v.value else v
                        } else if (field is APIField.G) field.getter(row)
                        else continue

                data[field.name] = value
            }

            data
        }
    }

    path(pattern) {
        get("/") {
            val (limit, offset) = ctx.limitAndOffset
            val parseResult = parseFields(ctx)
            val rows = transaction {
                model
                        .slice(*parseResult.columns.toTypedArray())
                        .run { model.getAllSelectExpression?.invoke(ctx)?.let { select { it } } ?: selectAll() }
                        .limit(limit + 1, offset.toLong())
                        .toList()
            }

            val hasMore = rows.count() == limit + 1
            val items = (if (hasMore) rows.dropLast(1) else rows).map(parseResult.resultRowMapper)

            mapOf(
                    "hasMore" to hasMore,
                    "items" to items
            )
        }

        get("/{id}") {
            val parseResult = parseFields(ctx)
            val row = transaction {
                model
                        .slice(*parseResult.columns.toTypedArray())
                        .select(model.getOneSelectExpression(ctx.path("id")))
                        .limit(1)
                        .firstOrNull()
            }

            if (row == null) ctx.responseCode = StatusCode.NOT_FOUND
            mapOf("data" to row?.let { parseResult.resultRowMapper(it) })
        }

        // TODO: Allow updates
    }
}

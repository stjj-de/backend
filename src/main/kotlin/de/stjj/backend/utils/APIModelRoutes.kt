package de.stjj.backend.utils

import de.stjj.backend.models.User
import de.stjj.backend.models.hasHigherOrEqualRole
import de.stjj.backend.routes.api.APIException
import io.jooby.Context
import io.jooby.Kooby
import io.jooby.StatusCode
import io.jooby.Value
import org.jetbrains.exposed.dao.DaoEntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction

interface APIModel {
    val writePermissionChecker: (ctx: Context, idValue: Value?) -> Unit
    val defaultFields: String
    val apiFields: Set<APIField>
    val buildWhereCondition: (idValue: Value) -> Op<Boolean>
    val buildSelectAllWhereCondition: ((ctx: Context) -> Op<Boolean>?)? get() = null

    fun applyData(ctx: Context, it: UpdateBuilder<Int>, isUpdate: Boolean)
    fun getCreatedResponseData(ctx: Context, resultRow: ResultRow): Any? = null

    class InvalidResourceIDException(
            message: String = "The specified resource ID is invalid."
    ): APIException(StatusCode.BAD_REQUEST, "INVALID_RESOURCE_ID", message)

    companion object {
        fun minimumRole(role: User.Role): (Context, Value?) -> Unit {
            return { ctx: Context, _ ->
                if (!ctx.user.hasHigherOrEqualRole(role))
                    throw InsufficientPermissionsException(
                            "You are not allowed to perform this operation for this entity.",
                            mapOf(
                                    "requiredRole" to role,
                                    "yourRole" to ctx.user?.role
                            )
                    )
            }
        }

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

sealed class APIField(val name: String, val roleForReading: User.Role?) {
    class C(name: String, val column: Column<*>, val sortable: Boolean = false, roleForReading: User.Role? = null): APIField(name, roleForReading)
    class G(name: String, val dependsOn: Set<Column<*>> = emptySet(), roleForReading: User.Role? = null, val getter: (ResultRow) -> Any?): APIField(name, roleForReading)
}

class UnknownFieldException(fieldName: String): APIException(
        StatusCode.BAD_REQUEST,
        "UNKNOWN_FIELD",
        "There is no field named $fieldName on this model.",
        details = mapOf("fieldName" to fieldName)
)

class FieldNotAllowedForSortingException(fieldName: String): APIException(
        StatusCode.BAD_REQUEST,
        "FIELD_NOT_ALLOWED_FOR_SORTING",
        "You can not sort by the field named $fieldName.",
        details = mapOf("fieldName" to fieldName)
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

    fun getAPIField(name: String) = model.apiFields.find { it.name == name } ?: throw UnknownFieldException(name)

    fun getSortColumn(ctx: Context): APIField.C? {
        val name = ctx.query("sortBy").valueOrNull()
        val column = name?.let { getAPIField(it) } ?: return null
        if (column !is APIField.C || !column.sortable) throw FieldNotAllowedForSortingException(name)
        return column
    }

    fun parseFields(ctx: Context): ParseResult {
        val fieldNames = ctx.query("fields").value(model.defaultFields).split(",")
        val requestedFields = fieldNames.map(::getAPIField)

        for (field in requestedFields) {
            if (!ctx.user.hasHigherOrEqualRole(field.roleForReading)) throw FieldAccessNotAllowedException(field.name)
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
            val sortColumn = getSortColumn(ctx)
            val order = if (ctx.query("asc").booleanValue(true)) SortOrder.ASC else SortOrder.DESC
            val rows = transaction {
                model
                        .slice(*parseResult.columns.toTypedArray())
                        .run { model.buildSelectAllWhereCondition?.invoke(ctx)?.let { select { it } } ?: selectAll() }
                        .run { sortColumn?.let { orderBy(it.column, order) } ?: this }
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
                        .select(model.buildWhereCondition(ctx.path("id")))
                        .limit(1)
                        .firstOrNull()
            }

            if (row == null) ctx.responseCode = StatusCode.NOT_FOUND
            mapOf("data" to row?.let { parseResult.resultRowMapper(it) })
        }

        post("/") {
            model.writePermissionChecker(ctx, null)
            val result = transaction { model.insert { model.applyData(ctx, it, false) }.resultedValues!!.first() }
            ctx.responseCode = StatusCode.CREATED
            mapOf("data" to model.getCreatedResponseData(ctx, result))
        }

        put("/{id}") {
            val idValue = ctx.path("id")
            model.writePermissionChecker(ctx, idValue)
            transaction { model.update({ model.buildWhereCondition(idValue) }, 1) { model.applyData(ctx, it, true) } }
            Unit
        }

        delete("/{id}") {
            val idValue = ctx.path("id")
            model.writePermissionChecker(ctx, idValue)
            transaction { model.deleteWhere(1) { model.buildWhereCondition(idValue) } }
            Unit
        }
    }
}

package de.stjj.backend.utils

import de.stjj.backend.models.User
import de.stjj.backend.routes.api.APIException
import io.jooby.Kooby
import io.jooby.StatusCode
import org.jetbrains.exposed.dao.DaoEntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

interface APIModel {
    val writeAllowedRole: User.Role
    val defaultFields: String
    val apiFields: Set<APIField>
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

class FieldAccessNotAllowedException(fieldName: String): APIException(
        StatusCode.FORBIDDEN,
        "FIELD_ACCESS_NOT_ALLOWED",
        "You are not allowed to access the field named $fieldName on this model.",
        mapOf("fieldName" to fieldName)
)

@ExperimentalStdlibApi
fun Kooby.apiModelRoutes(pattern: String, model: APIModel) {
    if (model !is Table) throw Error("${model::class.simpleName} is not a Table")

    path(pattern) {
        get("/") {
            val (limit, offset) = ctx.limitAndOffset

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

            val rows = transaction {
                model
                        .slice(*columns.toTypedArray())
                        .selectAll()
                        .limit(limit + 1, offset.toLong())
                        .toList()
            }

            val hasMore = rows.count() == limit + 1
            val items = (if (hasMore) rows.dropLast(1) else rows).map { row ->
                requestedFields.mapNotNull { field ->
                    val fieldValue =
                            if (field is APIField.C) {
                                val value = row[field.column]
                                if (value is DaoEntityID<*>) value.value else value
                            } else if (field is APIField.G) field.getter(row)
                            else null

                    fieldValue?.let { value -> field.name to value }
                }.toMap()
            }

            mapOf(
                    "hasMore" to hasMore,
                    "items" to items
            )
        }
    }
}

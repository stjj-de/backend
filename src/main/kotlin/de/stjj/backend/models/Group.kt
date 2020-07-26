package de.stjj.backend.models

import de.stjj.backend.utils.APIField
import de.stjj.backend.utils.APIModel
import de.stjj.backend.utils.user
import io.jooby.Context
import io.jooby.Value
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction

object Groups: IntIdTable("groups"), APIModel {
    val title = varchar("title", 255)
    val description = text("description") // HTML content

    private val minimumRoleChecker = APIModel.minimumRole(User.Role.EDITOR)
    override val writePermissionChecker = { ctx: Context, idValue: Value? ->
        transaction {
            if (ctx.method != "DELETE" && idValue != null) {
                val group = Group.findById(idValue.intValue()) ?: throw APIModel.InvalidResourceIDException()
                if (ctx.user?.groups?.any { it.id == group.id } == true) return@transaction
            }

            minimumRoleChecker(ctx, idValue)
        }
    }
    override val defaultFields = "id,title,description"
    override val apiFields = setOf(
            APIField.C("id", id, true),
            APIField.C("title", title, true),
            APIField.C("description", description, false),
            APIField.G("members", setOf(id)) { result ->
                transaction { GroupMembers.slice(GroupMembers.user).select { GroupMembers.group eq result[Groups.id] }.map { it[GroupMembers.user].value } }
            }
    )
    override val buildWhereCondition = APIModel.createIntIDGetOneSelectExpression(Groups)
    override val buildSelectAllWhereCondition = fun(ctx: Context): Op<Boolean>? {
        val onlyOwn = ctx.query("onlyOwn").booleanValue(false)

        return if (ctx.user != null && onlyOwn) with(SqlExpressionBuilder) { Groups.id inList ctx.user!!.groups.map { it.id } }
        else null
    }

    override fun applyData(ctx: Context, it: UpdateBuilder<Int>, isUpdate: Boolean) {
        val data = ctx.body(CreateOrUpdateData::class.java)
        it[title] = data.title
        it[description] = data.description
    }

    data class CreateOrUpdateData(
            val title: String,
            val description: String
    )
}

object GroupMembers: Table("group_members") {
    val group = reference("group", Groups, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val user = reference("user", Users, ReferenceOption.CASCADE, ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(group, user)
}

class Group(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Group>(Groups)
    var title by Groups.title
    var description by Groups.description
    var members by User via GroupMembers
}

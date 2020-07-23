package de.stjj.backend.models

import de.stjj.backend.utils.APIField
import de.stjj.backend.utils.APIModel
import io.jooby.Context
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction

object Groups: IntIdTable("groups"), APIModel {
    val title = varchar("title", 255)
    val shortDescription = text("short_description")
    val pageContent = text("page_content") // HTML content

    override val writePermissionChecker = APIModel.minimumRole(User.Role.EDITOR)
    override val defaultFields = "id,title,shortDescription"
    override val apiFields = setOf(
            APIField.C("id", id, true),
            APIField.C("title", title, true),
            APIField.C("shortDescription", shortDescription, true),
            APIField.C("pageContent", pageContent, true),
            APIField.G("members", setOf(id)) { result ->
                transaction { GroupMembers.slice(GroupMembers.user).select { GroupMembers.group eq result[Groups.id] }.map { it[GroupMembers.user].value } }
            }
    )
    override val buildWhereCondition = APIModel.createIntIDGetOneSelectExpression(Groups)

    override fun applyData(ctx: Context, it: UpdateBuilder<Int>, isUpdate: Boolean) {
        val data = ctx.body(CreateOrUpdateData::class.java)
        it[title] = data.title
        it[shortDescription] = data.shortDescription
        it[pageContent] = data.pageContent
    }

    data class CreateOrUpdateData(
            val title: String,
            val shortDescription: String,
            val pageContent: String
    )
}

object GroupMembers: Table("group_members") {
    val group = reference("group", Groups)
    val user = reference("user", Users)

    override val primaryKey = PrimaryKey(group, user)
}

class Group(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Group>(Groups)
    var title by Groups.title
    var shortDescription by Groups.shortDescription
    var pageContent by Groups.pageContent
    var members by User via GroupMembers
}

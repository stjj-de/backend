package de.stjj.backend.models

import de.stjj.backend.utils.*
import io.jooby.Context
import io.jooby.Value
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDateTime

object Posts: IntIdTable("posts"), APIModel {
    val slug = varchar("slug", 50).uniqueIndex()
    val title = varchar("title", 255)
    val group = optReference("group", Groups, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val publishedAt = datetime("published_at").nullable()
    val relevantUntil = datetime("relevant_until").nullable()
    val excerpt = varchar("excerpt", 255)
    val content = text("content") // HTML content
    val author = optReference(
        "author",
        Users,
        ReferenceOption.SET_NULL,
        ReferenceOption.CASCADE
    )

    private val minimumRoleChecker = APIModel.minimumRole(User.Role.EDITOR)
    override val writePermissionChecker = { ctx: Context, idValue: Value? ->
        transaction {
            val post = idValue?.let { id -> Post.findById(id.intValue()) ?: throw APIModel.InvalidResourceIDException() }
            if (post == null || (post.group != null && ctx.user?.groups?.any { it.id == post.group!!.id } == true)) {
                if (ctx.method != "DELETE") {
                    val data = ctx.body(CreateOrUpdateData::class.java)
                    ctx.attribute("data", data)

                    if (ctx.user?.groups?.any { it.id.value == data.group } == false) minimumRoleChecker(ctx, idValue)
                }
            } else {
                minimumRoleChecker(ctx, idValue)
            }
        }
    }
    override val defaultFields = "slug,title,publishedAt,excerpt"
    override val apiFields = setOf(
            APIField.C("id", id, true),
            APIField.C("slug", slug, true),
            APIField.C("title", title, true),
            APIField.C("group", group, true),
            APIField.C("publishedAt", publishedAt, true),
            APIField.C("relevantUntil", relevantUntil, true),
            APIField.C("excerpt", excerpt),
            APIField.C("content", content),
            APIField.C("author", author)
    )

    override val buildWhereCondition = fun(idValue: Value): Op<Boolean> {
        val stringValue = idValue.value()
        return with(SqlExpressionBuilder) {
            if (stringValue.startsWith("_")) slug eq stringValue.drop(1)
            else Posts.id eq idValue.intValue()
        }
    }

    override val buildSelectAllWhereCondition = fun(ctx: Context): Op<Boolean>? {
        val onlyRelevant = ctx.query("onlyRelevant").booleanValue(false)
        val onlyPublished = ctx.query("onlyPublished").booleanValue(true)
        val groupValue = ctx.query("group")
        val onlyOwnGroup = groupValue.valueOrNull() == "own"

        if (
                !onlyPublished &&
                (ctx.user == null || !ctx.user!!.role.isHigherOrEqual(User.Role.EDITOR) && !onlyOwnGroup)
        ) throw InsufficientPermissionsException("You are not allowed to access posts which were not published yet.")

        return with(SqlExpressionBuilder) {
            mutableListOf<Op<Boolean>>().apply {
                addIf(onlyRelevant) { relevantUntil.isNull() or (relevantUntil greater LocalDateTime.now()) }
                addIf(onlyPublished) { publishedAt lessEq LocalDateTime.now() }
                addIf(!groupValue.isMissing) {
                    if (onlyOwnGroup) group inList ctx.user!!.groups.map { it.id }
                    else group eq if (groupValue.value() == "general") null else groupValue.intValue()
                }
            }.combine()
        }
    }

    override fun applyData(ctx: Context, it: UpdateBuilder<Int>, isUpdate: Boolean) {
        val data = ctx.attribute("data") as CreateOrUpdateData? ?: ctx.body(CreateOrUpdateData::class.java)

        val groupID = data.group?.let { id ->
            (transaction { Groups.slice(Groups.id).select { Groups.id eq id }.firstOrNull() }
                    ?: throw APIModel.InvalidResourceIDException("There is no group with the ID ${data.group}."))[Groups.id]
        }

        it[slug] = data.slug
        it[title] = data.title
        it[publishedAt] = data.publishedAt?.asLocalDateTime()
        it[relevantUntil] = data.relevantUntil?.asLocalDateTime()
        it[excerpt] = data.excerpt
        it[content] = data.content
        it[group] = groupID

        if (!isUpdate) it[author] = ctx.userEntityID!!
    }

    override fun getCreatedResponseData(ctx: Context, resultRow: ResultRow): Any? = mapOf("id" to resultRow[id].value)

    data class CreateOrUpdateData(
            val slug: String,
            val title: String,
            val group: Int?,
            val publishedAt: Instant?,
            val relevantUntil: Instant?,
            val excerpt: String,
            val content: String
    )
}

class Post(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Post>(Posts)
    var slug by Posts.slug
    var title by Posts.title
    var group by Group optionalReferencedOn Posts.group
    var publishedAt by Posts.publishedAt
    var relevantUntil by Posts.relevantUntil
    var excerpt by Posts.excerpt
    var content by Posts.content
    var author by Posts.author
}

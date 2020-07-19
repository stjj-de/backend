package de.stjj.backend.models

import de.stjj.backend.utils.*
import io.jooby.Context
import io.jooby.Value
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import java.time.Instant
import java.time.LocalDateTime

object Posts: IntIdTable("posts"), APIModel {
    val slug = varchar("slug", 50).uniqueIndex()
    val title = varchar("title", 255)
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

    override val defaultFields = "slug,title,publishedAt,excerpt"
    override val apiFields = setOf(
            APIField.C("id", id, true),
            APIField.C("slug", slug, true),
            APIField.C("title", title, true),
            APIField.C("publishedAt", publishedAt, true),
            APIField.C("relevantUntil", relevantUntil, true),
            APIField.C("excerpt", excerpt),
            APIField.C("content", content),
            APIField.C("author", author)
    )
    override val writeAllowedRole = User.Role.EDITOR
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

        if (!onlyPublished && ctx.user?.role?.isHigherOrEqual(User.Role.EDITOR) != true)
            throw InsufficientPermissionsException("You are not allowed to access posts which were not published yet.")

        return if (onlyPublished || onlyRelevant) with(SqlExpressionBuilder) {
            mutableListOf<Op<Boolean>>().apply {
                addIf(onlyRelevant) { relevantUntil greater LocalDateTime.now() }
                addIf(onlyPublished) { publishedAt lessEq LocalDateTime.now() }
            }.combine()
        } else null
    }

    override fun applyData(ctx: Context, it: UpdateBuilder<Int>, isUpdate: Boolean) {
        val data = ctx.body(CreateOrUpdateData::class.java)
        it[slug] = data.slug
        it[title] = data.title
        it[publishedAt] = data.publishedAt?.asLocalDateTime()
        it[relevantUntil] = data.relevantUntil?.asLocalDateTime()
        it[excerpt] = data.excerpt
        it[content] = data.content

        if (!isUpdate) it[author] = ctx.userEntityID!!
    }

    override fun getCreatedResponseData(ctx: Context, resultRow: ResultRow): Any? = mapOf("id" to resultRow[id].value)

    data class CreateOrUpdateData(
            val slug: String,
            val title: String,
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
    var publishedAt by Posts.publishedAt
    var relevantUntil by Posts.relevantUntil
    var excerpt by Posts.excerpt
    var content by Posts.content
    var author by Posts.author
}

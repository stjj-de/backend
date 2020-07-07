package de.stjj.backend.models

import de.stjj.backend.utils.*
import io.jooby.Context
import io.jooby.Value
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

object Posts: IntIdTable("posts"), APIModel {
    val slug = varchar("slug", 50)
    val title = varchar("title", 255)
    val publishedAt = datetime("published_at").nullable()
    val relevantUntil = datetime("relevant_until").nullable()
    val excerpt = varchar("excerpt", 255)
    val content = text("content") // HTML content

    override val defaultFields = "slug,title,publishedAt,excerpt"
    override val apiFields = setOf(
            APIField.C("id", id),
            APIField.C("slug", slug),
            APIField.C("title", title),
            APIField.C("publishedAt", publishedAt),
            APIField.C("relevantUntil", relevantUntil),
            APIField.C("excerpt", excerpt),
            APIField.C("content", content),
            APIField.G("authors", setOf(id)) { row ->
                transaction {
                    PostAuthors
                            .select { PostAuthors.post eq row[Posts.id] }
                            .map { r -> r[PostAuthors.author].value } }
            }
    )
    override val writeAllowedRole = User.Role.EDITOR
    override val getOneSelectExpression = fun(idValue: Value): Op<Boolean> {
        val stringValue = idValue.value()
        return with(SqlExpressionBuilder) {
            if (stringValue.startsWith("_")) slug eq stringValue.drop(1)
            else Posts.id eq idValue.intValue()
        }
    }
    override val getAllSelectExpression = fun(ctx: Context): Op<Boolean>? {
        val onlyRelevant = ctx.query("onlyRelevant").booleanValue(false)
        val onlyPublished = ctx.query("onlyPublished").booleanValue(true)

        if (!onlyPublished && ctx.user?.role?.isCompatible(User.Role.EDITOR) != true)
            throw InsufficientPermissionsException("You are not allowed to access posts which were not published yet.")

        return if (onlyPublished || onlyRelevant) with(SqlExpressionBuilder) {
            mutableListOf<Op<Boolean>>().apply {
                addIf(onlyRelevant) { relevantUntil greater LocalDateTime.now() }
                addIf(onlyPublished) { publishedAt lessEq LocalDateTime.now() }
            }.combine()
        } else null
    }
}

class Post(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Post>(Posts)
    var slug by Posts.slug
    var title by Posts.title
    var publishedAt by Posts.publishedAt
    var relevantUntil by Posts.relevantUntil
    var excerpt by Posts.excerpt
    var content by Posts.content

    var authors by User via PostAuthors
}

object PostAuthors: Table("post_authors") {
    val post = reference("post", Posts)
    val author = reference("author", Users)
    override val primaryKey = PrimaryKey(post, author)
}

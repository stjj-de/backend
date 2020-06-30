package de.stjj.backend.models

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.datetime

object Posts: IntIdTable("posts") {
    val slug = varchar("slug", 50)
    val title = varchar("title", 255)
    val publishedAt = datetime("published_at").nullable()
    val relevantUntil = datetime("relevant_until").nullable()
    val excerpt = varchar("excerpt", 255)
    val content = text("content") // HTML content
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

object PostAuthors: Table() {
    val post = reference("post", Posts)
    val author = reference("author", Users)
    override val primaryKey = PrimaryKey(post, author)
}

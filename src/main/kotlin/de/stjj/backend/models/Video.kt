package de.stjj.backend.models

import de.stjj.backend.utils.APIField
import de.stjj.backend.utils.APIModel
import de.stjj.backend.utils.InsufficientPermissionsException
import de.stjj.backend.utils.user
import io.jooby.Context
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.`java-time`.datetime
import java.time.LocalDateTime

object Videos: IntIdTable("videos"), APIModel {
    val title = varchar("title", 255)
    val publishedAt = datetime("published_at").nullable()
    val youtubeVideoID = varchar("youtube_video_id", 20)

    override val defaultFields = "id,title,publishedAt,youtubeVideoID"
    override val writeAllowedRole = User.Role.EDITOR
    override val apiFields = setOf(
            APIField.C("id", id, true),
            APIField.C("title", title, true),
            APIField.C("publishedAt", publishedAt, true),
            APIField.C("youtubeVideoID", youtubeVideoID)
    )
    override val getOneSelectExpression = APIModel.createIntIDGetOneSelectExpression(Videos)
    override val getAllSelectExpression = fun(ctx: Context): Op<Boolean>? {
        val onlyPublished = ctx.query("onlyPublished").booleanValue(true)

        if (!onlyPublished && ctx.user?.role?.isCompatible(User.Role.EDITOR) != true)
            throw InsufficientPermissionsException("You are not allowed to access videos which were not published yet.")

        return if (onlyPublished) with(SqlExpressionBuilder) { publishedAt lessEq LocalDateTime.now() }
        else null
    }
}

class Video(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Video>(Videos)
    var title by Videos.title
    var publishedAt by Videos.publishedAt
    var youtubeVideoID by Videos.youtubeVideoID
}

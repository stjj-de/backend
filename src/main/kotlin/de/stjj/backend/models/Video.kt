package de.stjj.backend.models

import de.stjj.backend.routes.api.APIException
import de.stjj.backend.utils.*
import io.jooby.Context
import io.jooby.StatusCode
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import java.time.Instant
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
    override val buildWhereCondition = APIModel.createIntIDGetOneSelectExpression(Videos)
    override val buildSelectAllWhereCondition = fun(ctx: Context): Op<Boolean>? {
        val onlyPublished = ctx.query("onlyPublished").booleanValue(true)

        if (!onlyPublished && ctx.user?.role?.isHigherOrEqual(User.Role.EDITOR) != true)
            throw InsufficientPermissionsException("You are not allowed to access videos which were not published yet.")

        return if (onlyPublished) with(SqlExpressionBuilder) { publishedAt lessEq LocalDateTime.now() }
        else null
    }

    override fun applyData(ctx: Context, it: UpdateBuilder<Int>, isUpdate: Boolean) {
        if (isUpdate) {
            val data = ctx.body(UpdateData::class)

            it[title] = data.title
            it[publishedAt] = data.publishedAt.asLocalDateTime()
        } else {
            val data = ctx.body(CreateData::class)
            val title = YouTubeAPI.getVideoTitle(data.youtubeVideoID)
                ?: throw APIException(StatusCode.NOT_FOUND, "INVALID_YOUTUBE_VIDEO_ID", "The video with the specified ID could not be found on YouTube.")

            it[Videos.title] = title
            it[publishedAt] = null
            it[youtubeVideoID] = data.youtubeVideoID
        }
    }

    data class CreateData(val youtubeVideoID: String)
    data class UpdateData(val title: String, val publishedAt: Instant)
}

class Video(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Video>(Videos)
    var title by Videos.title
    var publishedAt by Videos.publishedAt
    var youtubeVideoID by Videos.youtubeVideoID
}

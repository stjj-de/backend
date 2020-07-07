package de.stjj.backend.models

import de.stjj.backend.utils.APIField
import de.stjj.backend.utils.APIModel
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.`java-time`.datetime

object Videos: IntIdTable("videos"), APIModel {
    val title = varchar("title", 255)
    val publishedAt = datetime("published_at").nullable()
    val youtubeVideoID = varchar("youtube_video_id", 20)

    override val defaultFields = "id,title,publishedAt,youtubeVideoID"
    override val writeAllowedRole = User.Role.EDITOR
    override val apiFields = setOf(
            APIField.C("id", id),
            APIField.C("title", title),
            APIField.C("publishedAt", publishedAt),
            APIField.C("youtubeVideoID", youtubeVideoID)
    )
    override val getOneSelectExpression = APIModel.createIntIDGetOneSelectExpression(Videos)
}

class Video(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Video>(Videos)
    var title by Videos.title
    var publishedAt by Videos.publishedAt
    var youtubeVideoID by Videos.youtubeVideoID
}

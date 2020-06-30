package de.stjj.backend.models

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.`java-time`.datetime

object Videos: IntIdTable("videos") {
    val title = varchar("title", 255)
    val publishedAt = datetime("published_at").nullable()
    val youtubeVideoID = varchar("youtube_video_id", 20)
}

class Video(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Video>(Videos)
    var title by Videos.title
    var publishedAt by Videos.publishedAt
    var youtubeVideoID by Videos.youtubeVideoID
}

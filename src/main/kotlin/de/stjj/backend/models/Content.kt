package de.stjj.backend.models

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable

object Contents: IdTable<Content.ID>("contents") {
    override val id = enumerationByName("id", 255, Content.ID::class).entityId()
    val content = text("content")
}

class Content(id: EntityID<ID>): Entity<Content.ID>(id) {
    companion object : EntityClass<ID, Content>(Contents)
    var content by Contents.content

    enum class ID(val file: Boolean = false) {
        HOMEPAGE_INTRODUCTION,
        HOMEPAGE_TOP,
        GEMEINDE,
        PFARRBRIEF(true),
        MESSDIENERPLAN(true),
        IMPRESSUM,
        PRIVACY_POLICY,
        ADMIN_NEWS,
        MEDIATHEK,
        LIVE_VIDEO_ID
    }
}

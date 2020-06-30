package de.stjj.backend.models

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.`java-time`.datetime

object Events: IntIdTable("events") {
    val title = varchar("title", 255)
    val creator = optReference(
            "creator",
            Users,
            ReferenceOption.SET_NULL,
            ReferenceOption.CASCADE
    )
    val color = enumerationByName("color", 255, Event.Color::class)
    val description = text("description") // HTML content
    val date = datetime("date")
    val endDate = datetime("end_date").nullable()
    val relatedPost = optReference(
            "related_post",
            Posts,
            ReferenceOption.SET_NULL,
            ReferenceOption.CASCADE
    )
}

class Event(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Event>(Events)
    var title by Events.title
    var creator by User optionalReferencedOn Events.creator
    var color by Events.color
    var description by Events.description
    var date by Events.date
    var endDate by Events.endDate
    var relatedPost by Post optionalReferencedOn Events.relatedPost

    enum class Color {
        GRAY, RED, ORANGE, YELLOW, GREEN, TEAL, BLUE, INDIGO, PURPLE, PINK;
    }
}

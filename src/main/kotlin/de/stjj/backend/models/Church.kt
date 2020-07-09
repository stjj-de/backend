package de.stjj.backend.models

import de.stjj.backend.utils.APIField
import de.stjj.backend.utils.APIModel
import io.jooby.Context
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Churches: IntIdTable("churches"), APIModel {
    val title = varchar("title", 255)

    override val defaultFields = "id,title"
    override val writeAllowedRole = User.Role.EDITOR
    override val apiFields = setOf(
            APIField.C("id", id, true),
            APIField.C("title", title, true)
    )
    override val getOneSelectExpression = APIModel.createIntIDGetOneSelectExpression(Churches)
    override fun create(ctx: Context) {
        TODO("Not yet implemented")
    }
}

class Church(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Church>(Churches)
    var title by Churches.title
}

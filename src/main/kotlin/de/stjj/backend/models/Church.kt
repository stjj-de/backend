package de.stjj.backend.models

import de.stjj.backend.utils.APIField
import de.stjj.backend.utils.APIModel
import io.jooby.Context
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.statements.UpdateBuilder

object Churches: IntIdTable("churches"), APIModel {
    val title = varchar("title", 255)
    val googleMapsID = varchar("google_maps_id", 255)

    override val defaultFields = "id,title"
    override val writePermissionChecker = APIModel.minimumRole(User.Role.EDITOR)
    override val apiFields = setOf(
            APIField.C("id", id, true),
            APIField.C("title", title, true),
            APIField.C("googleMapsID", googleMapsID, false)
    )
    override val buildWhereCondition = APIModel.createIntIDGetOneSelectExpression(Churches)

    override fun applyData(ctx: Context, it: UpdateBuilder<Int>, isUpdate: Boolean) {
        val data = ctx.body(CreateOrUpdateData::class.java)
        it[title] = data.title
        it[googleMapsID] = data.googleMapsID
    }

    data class CreateOrUpdateData(
            val title: String,
            val googleMapsID: String
    )
}

class Church(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Church>(Churches)
    var title by Churches.title
    var googleMapsID by Churches.googleMapsID
}

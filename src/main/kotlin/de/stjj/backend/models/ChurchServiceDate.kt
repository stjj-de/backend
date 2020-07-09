package de.stjj.backend.models

import de.stjj.backend.utils.APIField
import de.stjj.backend.utils.APIModel
import io.jooby.Context
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.`java-time`.datetime

object ChurchServiceDates: IntIdTable("church_service_dates"), APIModel {
    val date = datetime("date")
    val church = reference("church", Churches)
    val description = text("description") // HTML content

    override val defaultFields = "id,date,church,description"
    override val writeAllowedRole = User.Role.EDITOR
    override val apiFields = setOf(
            APIField.C("id", id, true),
            APIField.C("date", date, true),
            APIField.C("church", church),
            APIField.C("description", description)
    )
    override val getOneSelectExpression = APIModel.createIntIDGetOneSelectExpression(ChurchServiceDates)
    override fun create(ctx: Context) {
        TODO("Not yet implemented")
    }
}

class ChurchServiceDate(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<ChurchServiceDate>(ChurchServiceDates)
    var date by ChurchServiceDates.date
    var church by Church referencedOn ChurchServiceDates.church
    var description by ChurchServiceDates.description
}

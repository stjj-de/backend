package de.stjj.backend.models

import de.stjj.backend.utils.APIField
import de.stjj.backend.utils.APIModel
import de.stjj.backend.utils.asLocalDateTime
import io.jooby.Context
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

object ChurchServiceDates: IntIdTable("church_service_dates"), APIModel {
    val date = datetime("date")
    val church = reference("church", Churches)
    val description = text("description") // HTML content

    override val defaultFields = "id,date,church"
    override val writePermissionChecker = APIModel.minimumRole(User.Role.EDITOR)
    override val apiFields = setOf(
            APIField.C("id", id, true),
            APIField.C("date", date, true),
            APIField.C("church", church),
            APIField.C("description", description)
    )

    override val buildWhereCondition = APIModel.createIntIDGetOneSelectExpression(ChurchServiceDates)
    override val buildSelectAllWhereCondition = { ctx: Context ->
        // Delete all church service dates that started at least an hour ago
        transaction { ChurchServiceDates.deleteWhere { date less LocalDateTime.now().minusHours(1) } }

        // Select all
        null
    }

    override fun applyData(ctx: Context, it: UpdateBuilder<Int>, isUpdate: Boolean) {
        val data = ctx.body(CreateOrUpdateData::class.java)

        val churchID = (transaction { Churches.slice(Churches.id).select { Churches.id eq data.church }.firstOrNull() }
                ?: throw APIModel.InvalidResourceIDException("There is no church with the ID ${data.church}."))[Churches.id]

        it[date] = data.date.asLocalDateTime().truncatedTo(ChronoUnit.MINUTES)
        it[church] = churchID
        it[description] = data.description
    }

    data class CreateOrUpdateData(
            val date: Instant,
            val church: Int,
            val description: String
    )
}

class ChurchServiceDate(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<ChurchServiceDate>(ChurchServiceDates)
    var date by ChurchServiceDates.date
    var church by Church referencedOn ChurchServiceDates.church
    var description by ChurchServiceDates.description
}

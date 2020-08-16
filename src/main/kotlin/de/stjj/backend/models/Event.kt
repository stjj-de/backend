package de.stjj.backend.models

import de.stjj.backend.routes.api.APIException
import de.stjj.backend.utils.APIField
import de.stjj.backend.utils.APIModel
import de.stjj.backend.utils.asLocalDateTime
import de.stjj.backend.utils.userEntityID
import io.jooby.Context
import io.jooby.StatusCode
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

class InvalidEventFilterException: APIException(
        StatusCode.BAD_REQUEST,
        "INVALID_EVENT_FILTER",
        "The specified filter does not match the required format. Allowed: yyyy-mm, yyyy-mm-dd and yyyy-mm-dd:yyyy-mm-dd."
)

object Events: IntIdTable("events"), APIModel {
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

    override val writePermissionChecker = APIModel.minimumRole(User.Role.EDITOR)
    override val defaultFields = "id,title,color,date,endDate"
    override val apiFields = setOf(
            APIField.C("id", id, true),
            APIField.C("title", title, true),
            APIField.C("creator", creator),
            APIField.C("color", color, true),
            APIField.C("description", description),
            APIField.C("date", date, true),
            APIField.C("endDate", endDate),
            APIField.C("relatedPost", relatedPost)
    )
    override val buildWhereCondition = APIModel.createIntIDGetOneSelectExpression(Events)
    override val buildSelectAllWhereCondition = fun(ctx: Context): Op<Boolean>? {
        val filter = ctx.query("filter").valueOrNull() ?: return null

        val startDate: LocalDate
        val endDate: LocalDate

        val dateStrings = filter.split(":")
        try {
            when (dateStrings.count()) {
                2 -> {
                    val startDateString = dateStrings[0]
                    val endDateString = dateStrings[1]
                    if (
                            startDateString.split("-").count() != 3 ||
                            endDateString.split("-").count() != 3
                    ) throw InvalidEventFilterException()

                    startDate = LocalDate.parse(startDateString)
                    endDate = LocalDate.parse(endDateString)
                }
                1 -> {
                    val dateString = dateStrings[0]

                    when (dateString.split("-").count()) {
                        2 -> {
                            // filter by month
                            val date = LocalDate.parse("$dateString-01").let { it.minusDays(it.dayOfMonth.toLong()) }

                            startDate = date
                            endDate = date.plusMonths(1)
                        }
                        3 -> {
                            // filter by day
                            startDate = LocalDate.parse(dateString)
                            endDate = startDate
                        }
                        else -> throw InvalidEventFilterException()
                    }
                }
                else -> throw InvalidEventFilterException()
            }

            return with(SqlExpressionBuilder) {
                date greaterEq startDate.atStartOfDay() and (date less endDate.plusDays(1).atStartOfDay())
            }
        } catch (e: DateTimeParseException) {
            throw InvalidEventFilterException()
        }
    }

    override fun applyData(ctx: Context, it: UpdateBuilder<Int>, isUpdate: Boolean) {
        val data = ctx.body(CreateOrUpdateData::class.java)

        val relatedPostID = data.relatedPost?.let { id ->
            (transaction { Posts.slice(Posts.id).select { Posts.id eq id }.firstOrNull() }
                    ?: throw APIModel.InvalidResourceIDException("There is no post with the ID ${data.relatedPost}."))[Posts.id]
        }

        if (!isUpdate) it[creator] = ctx.userEntityID

        it[title] = data.title
        it[color] = data.color
        it[description] = data.description
        it[date] = data.date.asLocalDateTime().truncatedTo(ChronoUnit.MINUTES)
        it[endDate] = data.endDate?.asLocalDateTime()?.truncatedTo(ChronoUnit.MINUTES)
        it[relatedPost] = relatedPostID
    }

    data class CreateOrUpdateData(
            val title: String,
            val color: Event.Color,
            val description: String,
            val date: Instant,
            val endDate: Instant?,
            val relatedPost: Int?
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

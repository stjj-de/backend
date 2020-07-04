package de.stjj.backend.models

import de.stjj.backend.utils.APIField
import de.stjj.backend.utils.APIModel
import io.jooby.Value
import io.jooby.exception.TypeMismatchException
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder

object Users: IntIdTable("users"), APIModel {
    val username = varchar("username", 30).uniqueIndex()
    val realName = varchar("real_name", 255)
    val displayName = varchar("display_name", 255).nullable()
    val position = varchar("position", 255).nullable()
    val role = enumerationByName("role", 255, User.Role::class)
    val imageID = char("image_id", 10).nullable()
    val passwordHash = char("password_hash", 60).nullable()
    val authToken = char("auth_token", 50).nullable().uniqueIndex()

    override val defaultFields = "id,displayName,imageID"
    override val writeAllowedRole = User.Role.EDITOR
    override val apiFields = setOf(
            APIField.C("id", id),
            APIField.C("username", username),
            APIField.C("realName", realName),
            APIField.C("position", position),
            APIField.C("role", role),
            APIField.C("imageID", imageID),
            APIField.G("displayName", setOf(realName, displayName)) { it[displayName] ?: it[realName] }
    )

    val getIDGetOneSelectExpression = APIModel.createIntIDGetOneSelectExpression(Users)

    override val getOneSelectExpression: (Value) -> Op<Boolean> = { idValue ->
        try {
            getIDGetOneSelectExpression(idValue)
        } catch (e: TypeMismatchException) {
            with(SqlExpressionBuilder) {
                username eq idValue.value()
            }
        }
    }
}

class User(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<User>(Users)

    var username by Users.username
    var realName by Users.realName
    var displayName by Users.displayName
    var position by Users.position
    var passwordHash by Users.passwordHash
    var role by Users.role
    var imageID by Users.imageID
    var authToken by Users.authToken

    enum class Role {
        NONE,
        EDITOR,
        ADMINISTRATOR
    }
}

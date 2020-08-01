package de.stjj.backend.models

import at.favre.lib.crypto.bcrypt.BCrypt
import de.stjj.backend.utils.APIField
import de.stjj.backend.utils.APIModel
import io.jooby.Context
import io.jooby.Value
import io.jooby.exception.TypeMismatchException
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction

private val bcryptHasher = BCrypt.withDefaults()

object Users: IntIdTable("users"), APIModel {
    val username = varchar("username", 30).uniqueIndex()
    val realName = varchar("real_name", 255)
    val displayName = varchar("display_name", 255).nullable()
    val image = reference(
            "image",
            UploadedFiles,
            ReferenceOption.SET_NULL,
            ReferenceOption.CASCADE
    ).nullable()
    val position = varchar("position", 255).nullable()
    val role = enumerationByName("role", 255, User.Role::class)
    val passwordHash = char("password_hash", 60)
    val authToken = char("auth_token", 50).nullable().uniqueIndex()

    override val writePermissionChecker = APIModel.minimumRole(User.Role.ADMINISTRATOR)
    override val defaultFields = "id,displayName,imageID"
    override val apiFields = setOf(
            APIField.C("id", id, true),
            APIField.C("username", username, true),
            APIField.C("realName", realName, true),
            APIField.C("image", image, false),
            APIField.C("position", position, true),
            APIField.C("role", role, true),
            APIField.G("displayName", setOf(realName, displayName)) { it[displayName] ?: it[realName] },
            APIField.G("groups", setOf(id)) { result ->
                transaction { GroupMembers.slice(GroupMembers.group).select { GroupMembers.user eq result[Users.id] }.map { it[GroupMembers.group].value } }
            }
    )
    val getIDGetOneSelectExpression = APIModel.createIntIDGetOneSelectExpression(Users)
    override val buildWhereCondition: (Value) -> Op<Boolean> = { idValue ->
        try {
            getIDGetOneSelectExpression(idValue)
        } catch (e: TypeMismatchException) {
            with(SqlExpressionBuilder) { username eq idValue.value() }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun applyData(ctx: Context, it: UpdateBuilder<Int>, isUpdate: Boolean) {
        val data = ctx.body(CreateOrUpdateData::class.java)

        it[username] = data.username
        it[realName] = data.realName
        it[position] = data.position
        it[role] = data.role

        if (data.password != null) it[passwordHash] = bcryptHasher.hash(12, data.password.toCharArray()).decodeToString()
    }

    data class CreateOrUpdateData(
        val username: String,
        val realName: String,
        val position: String,
        val role: User.Role,
        val password: String?
    )
}

class User(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<User>(Users)

    var username by Users.username
    var realName by Users.realName
    var displayName by Users.displayName
    var image by UploadedFile optionalReferencedOn Users.image
    var position by Users.position
    var passwordHash by Users.passwordHash
    var role by Users.role
    var authToken by Users.authToken
    var groups by Group via GroupMembers

    enum class Role {
        NONE,
        EDITOR,
        ADMINISTRATOR
    }
}

fun User?.hasHigherOrEqualRole(otherRole: User.Role?): Boolean = this?.role.isHigherOrEqual(otherRole)
fun User.Role?.isHigherOrEqual(otherRole: User.Role?): Boolean = (this?.ordinal ?: -1) >= (otherRole?.ordinal ?: -1)

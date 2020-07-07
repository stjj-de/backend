package de.stjj.backend.models

import de.stjj.backend.utils.APIField
import de.stjj.backend.utils.APIModel
import io.jooby.Value
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.`java-time`.datetime

object UploadedFiles: IdTable<String>("uploaded_files"), APIModel {
    override val id: Column<EntityID<String>> = char("id", 10).entityId()
    val title = varchar("title", 255)
    val mimeType = varchar("varchar", 255).nullable()
    val alias = varchar("alias", 50).uniqueIndex()
    val uploadedAt = datetime("uploaded_at")

    override val primaryKey = PrimaryKey(id)

    override val defaultFields = "id,title,mimeType"
    override val apiFields = setOf(
            APIField.C("id", id, true),
            APIField.C("title", title, true),
            APIField.C("mimeType", mimeType, true),
            APIField.C("alias", alias, true),
            APIField.C("uploadedAt", uploadedAt, true)
    )
    override val writeAllowedRole = User.Role.EDITOR
    override val getOneSelectExpression: (idValue: Value) -> Op<Boolean> = { idValue ->
        val id = idValue.value()
        with(SqlExpressionBuilder) { if (id.startsWith("_")) alias eq id else UploadedFiles.id eq id }
    }
}

class UploadedFile(id: EntityID<String>): Entity<String>(id) {
    companion object : EntityClass<String, UploadedFile>(UploadedFiles)
    var title by UploadedFiles.title
    var mimeType by UploadedFiles.mimeType
    var alias by UploadedFiles.alias
    var uploadedAt by UploadedFiles.uploadedAt
}

package de.stjj.backend.models

import de.stjj.backend.routes.api.APIException
import de.stjj.backend.utils.APIField
import de.stjj.backend.utils.APIModel
import io.jooby.Context
import io.jooby.StatusCode
import io.jooby.Value
import org.apache.tika.mime.MimeTypes
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder

object UploadedFiles: IdTable<String>("uploaded_files"), APIModel {
    override val id: Column<EntityID<String>> = char("id", 64).entityId()
    val title = varchar("title", 255).nullable()
    val mimeType = varchar("mime_type", 255).nullable()
    val uploadedAt = datetime("uploaded_at")
    val firstUploader = reference(
            "first_uploader",
            Users,
            ReferenceOption.SET_NULL,
            ReferenceOption.CASCADE
    ).nullable()

    override val primaryKey = PrimaryKey(id)

    override val writePermissionChecker = APIModel.minimumRole(User.Role.EDITOR)
    override val defaultFields = "title,mimeType"
    override val apiFields = setOf(
            APIField.C("id", id, true),
            APIField.C("title", title, true),
            APIField.C("mimeType", mimeType, true),
            APIField.C("uploadedAt", uploadedAt, true)
    )
    override val buildWhereCondition: (idValue: Value) -> Op<Boolean> = { idValue ->
        val id = idValue.value()
        with(SqlExpressionBuilder) { UploadedFiles.id eq id }
    }

    override fun applyData(ctx: Context, it: UpdateBuilder<Int>, isUpdate: Boolean) {
        if (!isUpdate) throw APIException(StatusCode.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "You can not create files using this endpoint. Use /files instead.")

        val data = ctx.body(UpdateData::class.java)
        it[title] = data.title
    }

    data class UpdateData(val title: String?)
}

class UploadedFile(id: EntityID<String>): Entity<String>(id) {
    companion object : EntityClass<String, UploadedFile>(UploadedFiles)
    var title by UploadedFiles.title
    var mimeTypeName by UploadedFiles.mimeType
    var uploadedAt by UploadedFiles.uploadedAt
    var firstUploader by UploadedFiles.firstUploader

    val mimeType by lazy { mimeTypeName?.let { MimeTypes.getDefaultMimeTypes().forName(it) } }
}

package de.stjj.backend.models

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.`java-time`.datetime

object UploadedFiles: IdTable<String>("uploaded_files") {
    override val id: Column<EntityID<String>> = char("id", 10).entityId()
    val title = varchar("title", 255)
    val mimeType = varchar("varchar", 255).nullable()
    val alias = varchar("alias", 50).uniqueIndex()
    val uploadedAt = datetime("uploaded_at")

    override val primaryKey = PrimaryKey(id)
}

class UploadedFile(id: EntityID<String>): Entity<String>(id) {
    companion object : EntityClass<String, UploadedFile>(UploadedFiles)
    var title by UploadedFiles.title
    var mimeType by UploadedFiles.mimeType
    var alias by UploadedFiles.alias
    var uploadedAt by UploadedFiles.uploadedAt
}

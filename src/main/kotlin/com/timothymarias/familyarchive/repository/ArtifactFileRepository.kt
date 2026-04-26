package com.timothymarias.familyarchive.repository

import com.timothymarias.familyarchive.database.ArtifactFiles
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime

data class ArtifactFileRecord(
    val id: Long,
    val artifactId: Long,
    val fileSequence: Int,
    val storagePath: String,
    val mimeType: String,
    val fileSize: Long,
    val thumbnailPath: String?,
    val thumbnailSize: String?,
    val uploadedAt: LocalDateTime,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

class ArtifactFileRepository {
    fun findById(id: Long): ArtifactFileRecord? =
        ArtifactFiles.selectAll().where { ArtifactFiles.id eq id }
            .map { it.toRecord() }
            .singleOrNull()

    fun findByArtifactId(artifactId: Long): List<ArtifactFileRecord> =
        ArtifactFiles.selectAll().where { ArtifactFiles.artifactId eq artifactId }
            .map { it.toRecord() }

    fun findByArtifactIdOrderBySequence(artifactId: Long): List<ArtifactFileRecord> =
        ArtifactFiles.selectAll().where { ArtifactFiles.artifactId eq artifactId }
            .orderBy(ArtifactFiles.fileSequence, SortOrder.ASC)
            .map { it.toRecord() }

    fun create(
        artifactId: Long,
        fileSequence: Int,
        storagePath: String,
        mimeType: String,
        fileSize: Long,
    ): Long =
        ArtifactFiles.insert {
            it[ArtifactFiles.artifactId] = artifactId
            it[ArtifactFiles.fileSequence] = fileSequence
            it[ArtifactFiles.storagePath] = storagePath
            it[ArtifactFiles.mimeType] = mimeType
            it[ArtifactFiles.fileSize] = fileSize
        }[ArtifactFiles.id].value

    fun updateThumbnail(id: Long, thumbnailPath: String?, thumbnailSize: String?) {
        ArtifactFiles.update({ ArtifactFiles.id eq id }) {
            it[ArtifactFiles.thumbnailPath] = thumbnailPath
            it[ArtifactFiles.thumbnailSize] = thumbnailSize
            it[updatedAt] = LocalDateTime.now()
        }
    }

    fun delete(id: Long) {
        ArtifactFiles.deleteWhere { ArtifactFiles.id eq id }
    }

    fun deleteByArtifactId(artifactId: Long) {
        ArtifactFiles.deleteWhere { ArtifactFiles.artifactId eq artifactId }
    }

    private fun ResultRow.toRecord() = ArtifactFileRecord(
        id = this[ArtifactFiles.id].value,
        artifactId = this[ArtifactFiles.artifactId].value,
        fileSequence = this[ArtifactFiles.fileSequence],
        storagePath = this[ArtifactFiles.storagePath],
        mimeType = this[ArtifactFiles.mimeType],
        fileSize = this[ArtifactFiles.fileSize],
        thumbnailPath = this[ArtifactFiles.thumbnailPath],
        thumbnailSize = this[ArtifactFiles.thumbnailSize],
        uploadedAt = this[ArtifactFiles.uploadedAt],
        createdAt = this[ArtifactFiles.createdAt],
        updatedAt = this[ArtifactFiles.updatedAt],
    )
}

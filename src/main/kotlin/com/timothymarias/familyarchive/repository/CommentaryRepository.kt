package com.timothymarias.familyarchive.repository

import com.timothymarias.familyarchive.database.Commentaries
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime

data class CommentaryRecord(
    val id: Long,
    val artifactId: Long,
    val commentaryText: String,
    val commentaryType: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

class CommentaryRepository {
    fun findById(id: Long): CommentaryRecord? =
        Commentaries.selectAll().where { Commentaries.id eq id }
            .map { it.toRecord() }
            .singleOrNull()

    fun findByArtifactId(artifactId: Long): List<CommentaryRecord> =
        Commentaries.selectAll().where { Commentaries.artifactId eq artifactId }
            .map { it.toRecord() }

    fun create(artifactId: Long, commentaryText: String, commentaryType: String?): Long =
        Commentaries.insert {
            it[Commentaries.artifactId] = artifactId
            it[Commentaries.commentaryText] = commentaryText
            it[Commentaries.commentaryType] = commentaryType
        }[Commentaries.id].value

    fun update(id: Long, commentaryText: String, commentaryType: String?) {
        Commentaries.update({ Commentaries.id eq id }) {
            it[Commentaries.commentaryText] = commentaryText
            it[Commentaries.commentaryType] = commentaryType
            it[updatedAt] = LocalDateTime.now()
        }
    }

    fun delete(id: Long) {
        Commentaries.deleteWhere { Commentaries.id eq id }
    }

    private fun ResultRow.toRecord() = CommentaryRecord(
        id = this[Commentaries.id].value,
        artifactId = this[Commentaries.artifactId].value,
        commentaryText = this[Commentaries.commentaryText],
        commentaryType = this[Commentaries.commentaryType],
        createdAt = this[Commentaries.createdAt],
        updatedAt = this[Commentaries.updatedAt],
    )
}

package com.timothymarias.familyarchive.repository

import com.timothymarias.familyarchive.database.Transcriptions
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime

data class TranscriptionRecord(
    val id: Long,
    val artifactId: Long,
    val transcriptionText: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

class TranscriptionRepository {
    fun findById(id: Long): TranscriptionRecord? =
        Transcriptions.selectAll().where { Transcriptions.id eq id }
            .map { it.toRecord() }
            .singleOrNull()

    fun findByArtifactId(artifactId: Long): TranscriptionRecord? =
        Transcriptions.selectAll().where { Transcriptions.artifactId eq artifactId }
            .map { it.toRecord() }
            .singleOrNull()

    fun create(artifactId: Long, transcriptionText: String): Long =
        Transcriptions.insert {
            it[Transcriptions.artifactId] = artifactId
            it[Transcriptions.transcriptionText] = transcriptionText
        }[Transcriptions.id].value

    fun update(id: Long, transcriptionText: String) {
        Transcriptions.update({ Transcriptions.id eq id }) {
            it[Transcriptions.transcriptionText] = transcriptionText
            it[updatedAt] = LocalDateTime.now()
        }
    }

    fun delete(id: Long) {
        Transcriptions.deleteWhere { Transcriptions.id eq id }
    }

    private fun ResultRow.toRecord() = TranscriptionRecord(
        id = this[Transcriptions.id].value,
        artifactId = this[Transcriptions.artifactId].value,
        transcriptionText = this[Transcriptions.transcriptionText],
        createdAt = this[Transcriptions.createdAt],
        updatedAt = this[Transcriptions.updatedAt],
    )
}

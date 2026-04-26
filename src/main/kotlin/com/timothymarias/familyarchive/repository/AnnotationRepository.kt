package com.timothymarias.familyarchive.repository

import com.timothymarias.familyarchive.database.Annotations
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.math.BigDecimal
import java.time.LocalDateTime

data class AnnotationRecord(
    val id: Long,
    val artifactFileId: Long,
    val annotationText: String,
    val xCoord: BigDecimal?,
    val yCoord: BigDecimal?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

class AnnotationRepository {
    fun findById(id: Long): AnnotationRecord? =
        Annotations.selectAll().where { Annotations.id eq id }
            .map { it.toRecord() }
            .singleOrNull()

    fun findByArtifactFileId(artifactFileId: Long): List<AnnotationRecord> =
        Annotations.selectAll().where { Annotations.artifactFileId eq artifactFileId }
            .map { it.toRecord() }

    fun create(artifactFileId: Long, annotationText: String, xCoord: BigDecimal?, yCoord: BigDecimal?): Long =
        Annotations.insert {
            it[Annotations.artifactFileId] = artifactFileId
            it[Annotations.annotationText] = annotationText
            it[Annotations.xCoord] = xCoord
            it[Annotations.yCoord] = yCoord
        }[Annotations.id].value

    fun update(id: Long, annotationText: String, xCoord: BigDecimal?, yCoord: BigDecimal?) {
        Annotations.update({ Annotations.id eq id }) {
            it[Annotations.annotationText] = annotationText
            it[Annotations.xCoord] = xCoord
            it[Annotations.yCoord] = yCoord
            it[updatedAt] = LocalDateTime.now()
        }
    }

    fun delete(id: Long) {
        Annotations.deleteWhere { Annotations.id eq id }
    }

    private fun ResultRow.toRecord() = AnnotationRecord(
        id = this[Annotations.id].value,
        artifactFileId = this[Annotations.artifactFileId].value,
        annotationText = this[Annotations.annotationText],
        xCoord = this[Annotations.xCoord],
        yCoord = this[Annotations.yCoord],
        createdAt = this[Annotations.createdAt],
        updatedAt = this[Annotations.updatedAt],
    )
}

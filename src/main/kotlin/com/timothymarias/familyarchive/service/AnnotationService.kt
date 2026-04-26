package com.timothymarias.familyarchive.service

import com.timothymarias.familyarchive.repository.AnnotationRecord
import com.timothymarias.familyarchive.repository.AnnotationRepository
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

class AnnotationService(
    private val annotationRepository: AnnotationRepository,
) {
    fun findById(id: Long): AnnotationRecord? = transaction { annotationRepository.findById(id) }

    fun findByArtifactFileId(artifactFileId: Long): List<AnnotationRecord> = transaction {
        annotationRepository.findByArtifactFileId(artifactFileId)
    }

    fun create(artifactFileId: Long, text: String, xCoord: BigDecimal?, yCoord: BigDecimal?): Long = transaction {
        annotationRepository.create(artifactFileId, text, xCoord, yCoord)
    }

    fun update(id: Long, text: String, xCoord: BigDecimal?, yCoord: BigDecimal?) = transaction {
        annotationRepository.update(id, text, xCoord, yCoord)
    }

    fun delete(id: Long) = transaction { annotationRepository.delete(id) }

    fun replaceAnnotations(artifactFileId: Long, annotations: List<AnnotationInput>) = transaction {
        val existing = annotationRepository.findByArtifactFileId(artifactFileId)
        val existingById = existing.associateBy { it.id }
        val incomingIds = annotations.mapNotNull { it.id }.toSet()

        // Delete removed annotations
        existing.filter { it.id !in incomingIds }.forEach { annotationRepository.delete(it.id) }

        // Update or create
        annotations.forEach { input ->
            if (input.id != null && existingById.containsKey(input.id)) {
                annotationRepository.update(input.id, input.text, input.xCoord, input.yCoord)
            } else {
                annotationRepository.create(artifactFileId, input.text, input.xCoord, input.yCoord)
            }
        }
    }
}

data class AnnotationInput(
    val id: Long? = null,
    val text: String,
    val xCoord: BigDecimal?,
    val yCoord: BigDecimal?,
)

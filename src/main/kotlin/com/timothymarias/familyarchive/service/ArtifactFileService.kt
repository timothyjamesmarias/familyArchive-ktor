package com.timothymarias.familyarchive.service

import com.timothymarias.familyarchive.repository.ArtifactFileRecord
import com.timothymarias.familyarchive.repository.ArtifactFileRepository
import org.jetbrains.exposed.sql.transactions.transaction

class ArtifactFileService(
    private val artifactFileRepository: ArtifactFileRepository,
) {
    fun findById(id: Long): ArtifactFileRecord? = transaction { artifactFileRepository.findById(id) }
    fun findByArtifactId(artifactId: Long): List<ArtifactFileRecord> = transaction { artifactFileRepository.findByArtifactId(artifactId) }
    fun findByArtifactIdOrderBySequence(artifactId: Long): List<ArtifactFileRecord> = transaction { artifactFileRepository.findByArtifactIdOrderBySequence(artifactId) }

    fun create(artifactId: Long, fileSequence: Int, storagePath: String, mimeType: String, fileSize: Long): Long = transaction {
        artifactFileRepository.create(artifactId, fileSequence, storagePath, mimeType, fileSize)
    }

    fun updateThumbnail(id: Long, thumbnailPath: String?, thumbnailSize: String?) = transaction {
        artifactFileRepository.updateThumbnail(id, thumbnailPath, thumbnailSize)
    }

    fun delete(id: Long) = transaction { artifactFileRepository.delete(id) }
    fun deleteByArtifactId(artifactId: Long) = transaction { artifactFileRepository.deleteByArtifactId(artifactId) }
}

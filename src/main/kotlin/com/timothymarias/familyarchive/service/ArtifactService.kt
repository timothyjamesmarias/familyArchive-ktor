package com.timothymarias.familyarchive.service

import com.timothymarias.familyarchive.model.ArtifactType
import com.timothymarias.familyarchive.repository.ArtifactFileRepository
import com.timothymarias.familyarchive.repository.ArtifactRecord
import com.timothymarias.familyarchive.repository.ArtifactRepository
import com.timothymarias.familyarchive.repository.Page
import com.timothymarias.familyarchive.repository.PageRequest
import com.timothymarias.familyarchive.service.storage.StorageService
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.io.InputStream

class ArtifactService(
    private val artifactRepository: ArtifactRepository,
    private val artifactFileRepository: ArtifactFileRepository,
    private val storageService: StorageService,
) {
    private val logger = LoggerFactory.getLogger(ArtifactService::class.java)

    fun findAll(pageRequest: PageRequest): Page<ArtifactRecord> = transaction { artifactRepository.findAll(pageRequest) }
    fun findById(id: Long): ArtifactRecord? = transaction { artifactRepository.findById(id) }
    fun findBySlug(slug: String): ArtifactRecord? = transaction { artifactRepository.findBySlug(slug) }

    fun findByIdWithAnnotations(id: Long, artifactType: ArtifactType): ArtifactRecord? = transaction {
        if (artifactType == ArtifactType.PHOTO) {
            artifactRepository.findPhotoByIdWithAnnotations(id)
        } else {
            artifactRepository.findById(id)
        }
    }

    fun findByType(artifactType: ArtifactType, pageRequest: PageRequest): Page<ArtifactRecord> = transaction {
        if (artifactType == ArtifactType.PHOTO) {
            artifactRepository.findPhotosByArtifactTypeWithAnnotations(artifactType, pageRequest)
        } else {
            artifactRepository.findByArtifactType(artifactType, pageRequest)
        }
    }

    fun findByTypeAndSlug(artifactType: ArtifactType, slug: String): ArtifactRecord? = transaction {
        if (artifactType == ArtifactType.PHOTO) {
            artifactRepository.findPhotoBySlugWithAnnotations(slug)
        } else {
            artifactRepository.findByArtifactTypeAndSlug(artifactType, slug)
        }
    }

    fun create(
        slug: String,
        artifactType: ArtifactType,
        title: String?,
        storagePath: String,
        mimeType: String,
        fileSize: Long,
        originalDateString: String?,
    ): Long = transaction {
        artifactRepository.create(slug, artifactType, title, storagePath, mimeType, fileSize, originalDateString)
    }

    fun update(id: Long, title: String?, artifactType: ArtifactType, originalDateString: String?) = transaction {
        artifactRepository.update(id, title, artifactType, originalDateString)
    }

    fun delete(id: Long): Boolean {
        val artifact = findById(id) ?: return false

        // Delete physical files from storage
        artifact.files.forEach { artifactFile ->
            try {
                storageService.delete(artifactFile.storagePath)
            } catch (e: Exception) {
                logger.error("Failed to delete file ${artifactFile.storagePath}", e)
            }
            artifactFile.thumbnailPath?.let { thumbnailPath ->
                try {
                    storageService.delete(thumbnailPath)
                } catch (e: Exception) {
                    logger.error("Failed to delete thumbnail $thumbnailPath", e)
                }
            }
        }

        if (artifact.files.isEmpty()) {
            try {
                storageService.delete(artifact.storagePath)
            } catch (e: Exception) {
                logger.error("Failed to delete legacy file ${artifact.storagePath}", e)
            }
        }

        transaction {
            artifactFileRepository.deleteByArtifactId(id)
            artifactRepository.delete(id)
        }
        logger.info("Deleted artifact $id")
        return true
    }

    fun addFileToArtifact(
        artifactId: Long,
        inputStream: InputStream,
        fileName: String,
        contentType: String,
        fileSize: Long,
    ): Long {
        val artifact = findById(artifactId)
            ?: throw IllegalArgumentException("Artifact not found with id: $artifactId")

        val maxSequence = artifact.files.maxOfOrNull { it.fileSequence } ?: 0
        val year = java.time.LocalDate.now().year
        val newSequence = maxSequence + 1
        val extension = fileName.substringAfterLast('.', "bin")
        val fileStoragePath = "artifacts/${artifact.artifactType.name.lowercase()}/$year/${artifact.slug}-$newSequence.$extension"

        val actualPath = storageService.store(inputStream, fileStoragePath, contentType, fileSize)

        val fileId = transaction {
            artifactFileRepository.create(artifactId, newSequence, actualPath, contentType, fileSize)
        }

        logger.info("Added file $newSequence to artifact $artifactId: $actualPath")
        return fileId
    }

    fun deleteArtifactFile(artifactId: Long, fileId: Long): Boolean {
        val artifact = findById(artifactId)
            ?: throw IllegalArgumentException("Artifact not found with id: $artifactId")

        val fileToDelete = artifact.files.find { it.id == fileId }
            ?: throw IllegalArgumentException("File not found with id: $fileId")

        if (artifact.files.size <= 1) {
            throw IllegalStateException("Cannot delete the only file. Please delete the entire artifact instead.")
        }

        transaction { artifactFileRepository.delete(fileId) }

        try {
            storageService.delete(fileToDelete.storagePath)
        } catch (e: Exception) {
            logger.error("Failed to delete file ${fileToDelete.storagePath}", e)
        }

        fileToDelete.thumbnailPath?.let { thumbnailPath ->
            try {
                storageService.delete(thumbnailPath)
            } catch (e: Exception) {
                logger.error("Failed to delete thumbnail $thumbnailPath", e)
            }
        }

        logger.info("Deleted file $fileId from artifact $artifactId")
        return true
    }

    fun count(): Long = transaction { artifactRepository.count() }

    companion object {
        fun shouldGenerateThumbnail(mimeType: String?): Boolean =
            mimeType != null && ThumbnailService.SUPPORTED_MIME_TYPES.contains(mimeType.lowercase())
    }
}

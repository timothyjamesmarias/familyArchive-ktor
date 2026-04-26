package com.timothymarias.familyarchive.service

import com.timothymarias.familyarchive.model.ArtifactType
import com.timothymarias.familyarchive.repository.ArtifactFileRepository
import com.timothymarias.familyarchive.repository.ArtifactRepository
import com.timothymarias.familyarchive.service.storage.StorageService
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.util.UUID

data class FileUpload(
    val inputStream: InputStream,
    val fileName: String,
    val contentType: String,
    val fileSize: Long,
)

class ArtifactUploadService(
    private val storageService: StorageService,
    private val artifactRepository: ArtifactRepository,
    private val artifactFileRepository: ArtifactFileRepository,
) {
    private val logger = LoggerFactory.getLogger(ArtifactUploadService::class.java)

    fun uploadArtifact(
        files: List<FileUpload>,
        artifactType: ArtifactType,
        title: String? = null,
        originalDateString: String? = null,
    ): Long {
        if (files.isEmpty()) throw IllegalArgumentException("At least one file is required")

        val slug = generateSlug()
        val primaryFile = files.first()
        val primaryExtension = primaryFile.fileName.substringAfterLast('.', "bin")
        val year = java.time.LocalDate.now().year
        val primaryStoragePath = "artifacts/${artifactType.name.lowercase()}/$year/$slug.$primaryExtension"

        val actualPrimaryPath = storageService.store(
            primaryFile.inputStream, primaryStoragePath, primaryFile.contentType, primaryFile.fileSize,
        )

        val artifactId = transaction {
            val id = artifactRepository.create(
                slug = slug,
                artifactType = artifactType,
                title = title,
                storagePath = actualPrimaryPath,
                mimeType = primaryFile.contentType,
                fileSize = primaryFile.fileSize,
                originalDateString = originalDateString,
            )

            files.forEachIndexed { index, file ->
                val extension = file.fileName.substringAfterLast('.', "bin")
                val fileStoragePath = if (index == 0) {
                    actualPrimaryPath
                } else {
                    val path = "artifacts/${artifactType.name.lowercase()}/$year/$slug-${index + 1}.$extension"
                    storageService.store(file.inputStream, path, file.contentType, file.fileSize)
                }

                artifactFileRepository.create(
                    artifactId = id,
                    fileSequence = index + 1,
                    storagePath = fileStoragePath,
                    mimeType = file.contentType,
                    fileSize = file.fileSize,
                )
            }

            id
        }

        logger.info("Uploaded artifact: $artifactId (${artifactType}) with ${files.size} file(s)")

        // JobRunr thumbnail enqueue will be added in Phase 8

        return artifactId
    }

    private fun generateSlug(): String {
        var slug: String
        var attempts = 0
        do {
            slug = UUID.randomUUID().toString().substring(0, 8)
            attempts++
        } while (transaction { artifactRepository.findBySlug(slug) } != null && attempts < 10)

        if (attempts >= 10) throw RuntimeException("Failed to generate unique slug after 10 attempts")
        return slug
    }
}

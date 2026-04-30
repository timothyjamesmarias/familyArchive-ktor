package com.timothymarias.familyarchive.jobs

import com.timothymarias.familyarchive.repository.ArtifactFileRepository
import com.timothymarias.familyarchive.repository.ArtifactRepository
import com.timothymarias.familyarchive.service.ThumbnailService
import org.jetbrains.exposed.sql.transactions.transaction
import org.jobrunr.jobs.annotations.Job
import org.jobrunr.jobs.context.JobContext
import org.slf4j.LoggerFactory

class ThumbnailGenerationJob(
    private val thumbnailService: ThumbnailService,
    private val artifactRepository: ArtifactRepository,
    private val artifactFileRepository: ArtifactFileRepository,
) {
    private val logger = LoggerFactory.getLogger(ThumbnailGenerationJob::class.java)

    @Job(name = "Generate Thumbnail", retries = 3)
    fun generateThumbnail(
        artifactId: Long,
        thumbnailSize: Int = ThumbnailService.THUMBNAIL_MEDIUM,
        jobContext: JobContext? = null,
    ) {
        logger.info("Starting thumbnail generation for artifact $artifactId (size: ${thumbnailSize}px)")

        transaction {
            val artifact = artifactRepository.findById(artifactId)
            if (artifact == null) {
                logger.warn("Artifact not found: $artifactId")
                return@transaction
            }

            val primaryFile = artifact.files.firstOrNull { it.fileSequence == 1 }
            if (primaryFile == null) {
                logger.debug("No ArtifactFile found for artifact $artifactId, skipping thumbnail generation")
                return@transaction
            }

            if (primaryFile.thumbnailPath != null) {
                logger.debug("Thumbnail already exists for artifact file ${primaryFile.id}, skipping")
                return@transaction
            }

            val result = thumbnailService.generateThumbnail(primaryFile, thumbnailSize)
            if (result != null) {
                artifactFileRepository.updateThumbnail(primaryFile.id, result.path, result.sizeSpec)
                logger.info("Successfully generated thumbnail for artifact file ${primaryFile.id}: ${result.path}")
            } else {
                logger.warn("Thumbnail generation returned null for artifact $artifactId")
            }
        }
    }

    @Job(name = "Regenerate Thumbnail", retries = 3)
    fun regenerateThumbnail(
        artifactId: Long,
        thumbnailSize: Int = ThumbnailService.THUMBNAIL_MEDIUM,
    ) {
        logger.info("Regenerating thumbnail for artifact $artifactId")

        transaction {
            val artifact = artifactRepository.findById(artifactId)
            if (artifact == null) {
                logger.warn("Artifact not found: $artifactId")
                return@transaction
            }

            val primaryFile = artifact.files.firstOrNull { it.fileSequence == 1 }
            if (primaryFile == null) {
                logger.debug("No ArtifactFile found for artifact $artifactId, skipping thumbnail regeneration")
                return@transaction
            }

            if (primaryFile.thumbnailPath != null) {
                thumbnailService.deleteThumbnail(primaryFile.thumbnailPath!!)
                artifactFileRepository.updateThumbnail(primaryFile.id, null, null)
            }

            val result = thumbnailService.generateThumbnail(primaryFile, thumbnailSize)
            if (result != null) {
                artifactFileRepository.updateThumbnail(primaryFile.id, result.path, result.sizeSpec)
                logger.info("Successfully regenerated thumbnail for artifact file ${primaryFile.id}: ${result.path}")
            }
        }
    }
}

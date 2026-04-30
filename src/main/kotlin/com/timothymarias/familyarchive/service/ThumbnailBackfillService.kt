package com.timothymarias.familyarchive.service

import com.timothymarias.familyarchive.jobs.ThumbnailGenerationJob
import com.timothymarias.familyarchive.repository.ArtifactRepository
import com.timothymarias.familyarchive.repository.PageRequest
import org.jetbrains.exposed.sql.transactions.transaction
import org.jobrunr.scheduling.JobScheduler
import org.slf4j.LoggerFactory

class ThumbnailBackfillService(
    private val artifactRepository: ArtifactRepository,
    private val jobScheduler: JobScheduler,
    private val thumbnailGenerationJob: ThumbnailGenerationJob,
) {
    private val logger = LoggerFactory.getLogger(ThumbnailBackfillService::class.java)

    fun backfillAllThumbnails(
        thumbnailSize: Int = ThumbnailService.THUMBNAIL_MEDIUM,
        batchSize: Int = 100,
    ): BackfillResult {
        logger.info("Starting thumbnail backfill (size: ${thumbnailSize}px, batch size: $batchSize)")

        val allArtifacts = transaction {
            artifactRepository.findAll(PageRequest(0, 10000)).content
        }

        val artifactsWithoutThumbnails = allArtifacts.filter { artifact ->
            artifact.files.isNotEmpty() &&
                artifact.files.any { file ->
                    file.fileSequence == 1 &&
                        file.thumbnailPath == null &&
                        ThumbnailService.SUPPORTED_MIME_TYPES.contains(file.mimeType.lowercase())
                }
        }

        val totalCount = artifactsWithoutThumbnails.size
        if (totalCount == 0) return BackfillResult(0, 0, 0)

        var jobsEnqueued = 0
        var skipped = 0

        artifactsWithoutThumbnails.chunked(batchSize).forEach { batch ->
            batch.forEach { artifact ->
                try {
                    val primaryFile = artifact.files.firstOrNull { it.fileSequence == 1 }
                    if (primaryFile == null || primaryFile.fileSize > ThumbnailService.MAX_SOURCE_SIZE_BYTES) {
                        skipped++
                        return@forEach
                    }
                    jobScheduler.enqueue { thumbnailGenerationJob.generateThumbnail(artifact.id, thumbnailSize, null) }
                    jobsEnqueued++
                } catch (e: Exception) {
                    logger.error("Failed to enqueue thumbnail job for artifact ${artifact.id}", e)
                    skipped++
                }
            }
        }

        return BackfillResult(totalCount, jobsEnqueued, skipped).also {
            logger.info("Thumbnail backfill complete: $it")
        }
    }

    fun regenerateAllThumbnails(
        thumbnailSize: Int = ThumbnailService.THUMBNAIL_MEDIUM,
        batchSize: Int = 100,
    ): BackfillResult {
        logger.info("Starting thumbnail regeneration (size: ${thumbnailSize}px)")

        val allArtifacts = transaction {
            artifactRepository.findAll(PageRequest(0, 10000)).content
        }

        val imageArtifacts = allArtifacts.filter { artifact ->
            artifact.files.isNotEmpty() &&
                artifact.files.any { file ->
                    file.fileSequence == 1 &&
                        ThumbnailService.SUPPORTED_MIME_TYPES.contains(file.mimeType.lowercase())
                }
        }

        val totalCount = imageArtifacts.size
        if (totalCount == 0) return BackfillResult(0, 0, 0)

        var jobsEnqueued = 0
        var skipped = 0

        imageArtifacts.chunked(batchSize).forEach { batch ->
            batch.forEach { artifact ->
                try {
                    val primaryFile = artifact.files.firstOrNull { it.fileSequence == 1 }
                    if (primaryFile == null || primaryFile.fileSize > ThumbnailService.MAX_SOURCE_SIZE_BYTES) {
                        skipped++
                        return@forEach
                    }
                    jobScheduler.enqueue { thumbnailGenerationJob.regenerateThumbnail(artifact.id, thumbnailSize) }
                    jobsEnqueued++
                } catch (e: Exception) {
                    logger.error("Failed to enqueue regeneration job for artifact ${artifact.id}", e)
                    skipped++
                }
            }
        }

        return BackfillResult(totalCount, jobsEnqueued, skipped).also {
            logger.info("Thumbnail regeneration complete: $it")
        }
    }

    fun getThumbnailStats(): ThumbnailStats {
        val allArtifacts = transaction {
            artifactRepository.findAll(PageRequest(0, 10000)).content
        }

        val imageArtifacts = allArtifacts.filter { artifact ->
            artifact.files.isNotEmpty() &&
                artifact.files.any { file ->
                    file.fileSequence == 1 &&
                        ThumbnailService.SUPPORTED_MIME_TYPES.contains(file.mimeType.lowercase())
                }
        }

        val withThumbnails = imageArtifacts.count { artifact ->
            artifact.files.firstOrNull { it.fileSequence == 1 }?.thumbnailPath != null
        }

        val tooLarge = imageArtifacts.count { artifact ->
            val primaryFile = artifact.files.firstOrNull { it.fileSequence == 1 }
            primaryFile != null && primaryFile.fileSize > ThumbnailService.MAX_SOURCE_SIZE_BYTES
        }

        return ThumbnailStats(
            totalArtifacts = allArtifacts.size.toLong(),
            totalImageArtifacts = imageArtifacts.size.toLong(),
            withThumbnails = withThumbnails.toLong(),
            withoutThumbnails = (imageArtifacts.size - withThumbnails).toLong(),
            tooLargeToProcess = tooLarge.toLong(),
        )
    }
}

data class BackfillResult(
    val totalArtifacts: Int,
    val jobsEnqueued: Int,
    val skipped: Int,
)

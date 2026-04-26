package com.timothymarias.familyarchive.service

import com.timothymarias.familyarchive.repository.ArtifactFileRecord
import com.timothymarias.familyarchive.service.storage.StorageService
import net.coobird.thumbnailator.Thumbnails
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ThumbnailService(
    private val storageService: StorageService,
) {
    private val logger = LoggerFactory.getLogger(ThumbnailService::class.java)

    companion object {
        const val THUMBNAIL_SMALL = 150
        const val THUMBNAIL_MEDIUM = 300
        const val THUMBNAIL_LARGE = 600
        const val MAX_SOURCE_SIZE_BYTES = 20L * 1024 * 1024

        val SUPPORTED_MIME_TYPES = setOf(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/bmp",
        )
    }

    fun generateThumbnail(artifactFile: ArtifactFileRecord, thumbnailSize: Int = THUMBNAIL_MEDIUM): ThumbnailResult? {
        if (!SUPPORTED_MIME_TYPES.contains(artifactFile.mimeType.lowercase())) {
            logger.debug("Skipping thumbnail generation for unsupported MIME type: ${artifactFile.mimeType}")
            return null
        }

        if (artifactFile.fileSize > MAX_SOURCE_SIZE_BYTES) {
            logger.warn("Source image too large for thumbnail generation: ${artifactFile.fileSize} bytes (max: $MAX_SOURCE_SIZE_BYTES)")
            return null
        }

        return try {
            val sourceStream = storageService.retrieve(artifactFile.storagePath)
            val thumbnailBytes = generateThumbnailBytes(sourceStream, thumbnailSize)
            val thumbnailPath = buildThumbnailPath(artifactFile.storagePath, thumbnailSize)

            val storedPath = storageService.store(
                ByteArrayInputStream(thumbnailBytes),
                thumbnailPath,
                artifactFile.mimeType,
                thumbnailBytes.size.toLong(),
            )

            logger.info("Generated thumbnail: $storedPath (${thumbnailBytes.size} bytes) for file ${artifactFile.id}")

            ThumbnailResult(path = storedPath, sizeSpec = "${thumbnailSize}x$thumbnailSize")
        } catch (e: Exception) {
            logger.error("Failed to generate thumbnail for artifact file ${artifactFile.id} at ${artifactFile.storagePath}", e)
            null
        }
    }

    private fun generateThumbnailBytes(inputStream: InputStream, size: Int): ByteArray {
        val outputStream = ByteArrayOutputStream()
        Thumbnails.of(inputStream)
            .size(size, size)
            .keepAspectRatio(true)
            .outputFormat("jpg")
            .outputQuality(0.85)
            .toOutputStream(outputStream)
        return outputStream.toByteArray()
    }

    private fun buildThumbnailPath(sourcePath: String, size: Int): String {
        val lastDotIndex = sourcePath.lastIndexOf('.')
        val pathWithoutExtension = if (lastDotIndex > 0) sourcePath.substring(0, lastDotIndex) else sourcePath
        return "${pathWithoutExtension}_thumb_${size}x$size.jpg"
    }

    fun deleteThumbnail(thumbnailPath: String) {
        try {
            if (storageService.exists(thumbnailPath)) {
                storageService.delete(thumbnailPath)
                logger.debug("Deleted thumbnail: $thumbnailPath")
            }
        } catch (e: Exception) {
            logger.error("Failed to delete thumbnail: $thumbnailPath", e)
        }
    }
}

data class ThumbnailResult(
    val path: String,
    val sizeSpec: String,
)

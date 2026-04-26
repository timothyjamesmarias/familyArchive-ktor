package com.timothymarias.familyarchive.service.storage

import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class LocalStorageService(
    private val config: LocalStorageConfig,
) : StorageService {
    private val logger = LoggerFactory.getLogger(LocalStorageService::class.java)
    private val uploadDir: Path

    init {
        uploadDir = Paths.get(config.uploadDir)
        try {
            Files.createDirectories(uploadDir)
            logger.info("Local storage initialized at: ${uploadDir.toAbsolutePath()}")
        } catch (e: Exception) {
            throw RuntimeException("Could not create upload directory!", e)
        }
    }

    override fun store(inputStream: InputStream, path: String, contentType: String, contentLength: Long): String {
        val destinationFile = uploadDir.resolve(path)
        Files.createDirectories(destinationFile.parent)
        inputStream.use {
            Files.copy(it, destinationFile, StandardCopyOption.REPLACE_EXISTING)
        }
        logger.debug("Stored file locally: $path")
        return path
    }

    override fun retrieve(path: String): InputStream {
        val file = uploadDir.resolve(path)
        if (!Files.exists(file)) {
            throw StorageException("File not found: $path")
        }
        return FileInputStream(file.toFile())
    }

    override fun delete(path: String) {
        val file = uploadDir.resolve(path)
        if (Files.exists(file)) {
            Files.delete(file)
            logger.debug("Deleted file locally: $path")
        }
    }

    override fun exists(path: String): Boolean = Files.exists(uploadDir.resolve(path))

    override fun generateUrl(path: String): String = "/uploads/$path"

    override fun generatePresignedUrl(path: String, expirationSeconds: Long): String = generateUrl(path)
}

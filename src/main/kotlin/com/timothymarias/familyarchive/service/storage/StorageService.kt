package com.timothymarias.familyarchive.service.storage

import java.io.InputStream

interface StorageService {
    fun store(inputStream: InputStream, path: String, contentType: String, contentLength: Long): String
    fun retrieve(path: String): InputStream
    fun delete(path: String)
    fun exists(path: String): Boolean
    fun generateUrl(path: String): String
    fun generatePresignedUrl(path: String, expirationSeconds: Long = 3600): String
}

class StorageException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

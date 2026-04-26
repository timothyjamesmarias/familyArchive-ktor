package com.timothymarias.familyarchive.service.storage

data class StorageConfig(
    val type: String = "local",
    val local: LocalStorageConfig = LocalStorageConfig(),
    val s3: S3StorageConfig = S3StorageConfig(),
)

data class LocalStorageConfig(
    val uploadDir: String = "uploads",
)

data class S3StorageConfig(
    val bucket: String = "",
    val region: String = "",
    val accessKey: String = "",
    val secretKey: String = "",
    val cloudFrontDomain: String = "",
    val publicBucket: Boolean = false,
)

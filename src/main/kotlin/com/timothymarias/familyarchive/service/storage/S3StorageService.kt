package com.timothymarias.familyarchive.service.storage

import org.slf4j.LoggerFactory
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.io.InputStream
import java.time.Duration

class S3StorageService(
    private val config: S3StorageConfig,
    s3Client: S3Client? = null,
    s3Presigner: S3Presigner? = null,
) : StorageService {
    private val logger = LoggerFactory.getLogger(S3StorageService::class.java)
    private val s3Client: S3Client
    private val s3Presigner: S3Presigner
    private val bucketName: String = config.bucket

    init {
        this.s3Client = s3Client ?: run {
            val credentials = AwsBasicCredentials.create(config.accessKey, config.secretKey)
            S3Client.builder()
                .region(Region.of(config.region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build()
        }

        this.s3Presigner = s3Presigner ?: run {
            val credentials = AwsBasicCredentials.create(config.accessKey, config.secretKey)
            S3Presigner.builder()
                .region(Region.of(config.region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build()
        }

        logger.info("S3 storage initialized with bucket: $bucketName in region: ${config.region}")
    }

    override fun store(inputStream: InputStream, path: String, contentType: String, contentLength: Long): String {
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(path)
            .contentType(contentType)
            .contentLength(contentLength)
            .build()

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength))
        logger.debug("Stored file in S3: s3://$bucketName/$path")
        return path
    }

    override fun retrieve(path: String): InputStream {
        val getObjectRequest = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(path)
            .build()

        return try {
            s3Client.getObject(getObjectRequest)
        } catch (e: NoSuchKeyException) {
            throw StorageException("File not found in S3: $path")
        }
    }

    override fun delete(path: String) {
        val deleteObjectRequest = DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(path)
            .build()

        s3Client.deleteObject(deleteObjectRequest)
        logger.debug("Deleted file from S3: s3://$bucketName/$path")
    }

    override fun exists(path: String): Boolean =
        try {
            val headObjectRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(path)
                .build()
            s3Client.headObject(headObjectRequest)
            true
        } catch (e: NoSuchKeyException) {
            false
        }

    override fun generateUrl(path: String): String =
        if (config.cloudFrontDomain.isNotBlank()) {
            "https://${config.cloudFrontDomain}/$path"
        } else if (config.publicBucket) {
            "https://$bucketName.s3.${config.region}.amazonaws.com/$path"
        } else {
            generatePresignedUrl(path, 3600)
        }

    override fun generatePresignedUrl(path: String, expirationSeconds: Long): String {
        val getObjectRequest = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(path)
            .build()

        val presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofSeconds(expirationSeconds))
            .getObjectRequest(getObjectRequest)
            .build()

        return s3Presigner.presignGetObject(presignRequest).url().toString()
    }
}

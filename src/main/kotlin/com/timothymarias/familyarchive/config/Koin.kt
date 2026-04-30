package com.timothymarias.familyarchive.config

import com.timothymarias.familyarchive.repository.AnnotationRepository
import com.timothymarias.familyarchive.repository.ArticleRepository
import com.timothymarias.familyarchive.repository.ArtifactFileRepository
import com.timothymarias.familyarchive.repository.ArtifactRepository
import com.timothymarias.familyarchive.repository.CommentaryRepository
import com.timothymarias.familyarchive.repository.FamilyMemberRepository
import com.timothymarias.familyarchive.repository.FamilyRepository
import com.timothymarias.familyarchive.repository.IndividualEventRepository
import com.timothymarias.familyarchive.repository.IndividualRepository
import com.timothymarias.familyarchive.repository.PlaceRepository
import com.timothymarias.familyarchive.repository.TranscriptionRepository
import com.timothymarias.familyarchive.repository.TranslationRepository
import com.timothymarias.familyarchive.repository.UserRepository
import com.timothymarias.familyarchive.service.AnnotationService
import com.timothymarias.familyarchive.service.ArticleService
import com.timothymarias.familyarchive.service.ArtifactFileService
import com.timothymarias.familyarchive.service.ArtifactService
import com.timothymarias.familyarchive.service.ArtifactUploadService
import com.timothymarias.familyarchive.service.FamilyTreeService
import com.timothymarias.familyarchive.service.FamilyTreeRelationshipService
import com.timothymarias.familyarchive.service.GedcomImporterService
import com.timothymarias.familyarchive.service.GedcomImportJobService
import com.timothymarias.familyarchive.service.ThumbnailBackfillService
import com.timothymarias.familyarchive.service.CommentaryService
import com.timothymarias.familyarchive.jobs.ThumbnailGenerationJob
import com.timothymarias.familyarchive.service.FamilyMemberService
import com.timothymarias.familyarchive.service.FamilyService
import com.timothymarias.familyarchive.service.IndividualEventService
import com.timothymarias.familyarchive.service.IndividualService
import com.timothymarias.familyarchive.service.PlaceService
import com.timothymarias.familyarchive.service.ThumbnailService
import com.timothymarias.familyarchive.service.TranscriptionService
import com.timothymarias.familyarchive.service.TranslationService
import com.timothymarias.familyarchive.service.UserService
import com.timothymarias.familyarchive.service.image.ImageProcessor
import com.timothymarias.familyarchive.service.image.ThumbnailatorImageProcessor
import com.timothymarias.familyarchive.service.storage.LocalStorageConfig
import com.timothymarias.familyarchive.service.storage.LocalStorageService
import com.timothymarias.familyarchive.service.storage.S3StorageConfig
import com.timothymarias.familyarchive.service.storage.S3StorageService
import com.timothymarias.familyarchive.service.storage.StorageService
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        modules(appModule(this@configureKoin))
    }
}

fun appModule(application: Application) = module {
    val config = application.environment.config

    // Repositories
    single { UserRepository() }
    single { IndividualRepository() }
    single { FamilyRepository() }
    single { FamilyMemberRepository() }
    single { PlaceRepository() }
    single { IndividualEventRepository() }
    single { AnnotationRepository() }
    single { ArticleRepository() }
    single { ArtifactFileRepository() }
    single { ArtifactRepository(get(), get()) }
    single { CommentaryRepository() }
    single { TranscriptionRepository() }
    single { TranslationRepository() }

    // Storage service (conditional on config)
    single<StorageService> {
        val storageType = config.propertyOrNull("storage.type")?.getString() ?: "local"
        when (storageType) {
            "s3" -> S3StorageService(
                S3StorageConfig(
                    bucket = config.property("storage.s3.bucket").getString(),
                    region = config.property("storage.s3.region").getString(),
                    accessKey = config.property("storage.s3.accessKey").getString(),
                    secretKey = config.property("storage.s3.secretKey").getString(),
                    cloudFrontDomain = config.propertyOrNull("storage.s3.cloudFrontDomain")?.getString() ?: "",
                    publicBucket = config.propertyOrNull("storage.s3.publicBucket")?.getString()?.toBoolean() ?: false,
                ),
            )
            else -> LocalStorageService(
                LocalStorageConfig(
                    uploadDir = config.propertyOrNull("storage.local.uploadDir")?.getString() ?: "uploads",
                ),
            )
        }
    }

    // Image processor
    single<ImageProcessor> { ThumbnailatorImageProcessor() }

    // Services
    single { UserService(get()) }
    single { IndividualService(get(), get(), get(), get()) }
    single { FamilyService(get()) }
    single { FamilyMemberService(get()) }
    single { PlaceService(get()) }
    single { IndividualEventService(get()) }
    single { AnnotationService(get()) }
    single { ArticleService(get()) }
    single { ArtifactFileService(get()) }
    single { ArtifactService(get(), get(), get()) }
    single { CommentaryService(get()) }
    single { TranscriptionService(get()) }
    single { TranslationService(get()) }
    single { ThumbnailService(get()) }
    single { ArtifactUploadService(get(), get(), get()) }
    single { FamilyTreeService(get(), get(), get(), get()) }
    single { GedcomImporterService(get(), get(), get(), get(), get()) }
    single { FamilyTreeRelationshipService(get(), get(), get(), get()) }

    // JobRunr (conditional on config)
    val jobrunrEnabled = config.propertyOrNull("jobrunr.enabled")?.getString()?.toBoolean() ?: true
    if (jobrunrEnabled) {
        single<javax.sql.DataSource> { appDataSource }
        single<org.jobrunr.storage.StorageProvider> {
            org.jobrunr.storage.sql.common.SqlStorageProviderFactory.using(get<javax.sql.DataSource>())
        }
        single<org.jobrunr.scheduling.JobScheduler> {
            val workerCount = config.propertyOrNull("jobrunr.workerCount")?.getString()?.toInt() ?: 4
            org.jobrunr.configuration.JobRunr.configure()
                .useStorageProvider(get())
                .useBackgroundJobServerIf(true, workerCount)
                .initialize()
                .jobScheduler
        }
        single { ThumbnailGenerationJob(get(), get(), get()) }
        single { ThumbnailBackfillService(get(), get(), get()) }
        single { GedcomImportJobService(get(), get()) }
    }
}

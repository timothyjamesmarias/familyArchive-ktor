package com.timothymarias.familyarchive.repository

import com.timothymarias.familyarchive.database.ArtifactFiles
import com.timothymarias.familyarchive.database.Annotations
import com.timothymarias.familyarchive.database.Artifacts
import com.timothymarias.familyarchive.model.ArtifactType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime

data class ArtifactRecord(
    val id: Long,
    val slug: String,
    val artifactType: ArtifactType,
    val title: String?,
    val storagePath: String,
    val mimeType: String,
    val fileSize: Long,
    val uploadedAt: LocalDateTime,
    val originalDateString: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val files: List<ArtifactFileRecord> = emptyList(),
)

class ArtifactRepository(
    private val artifactFileRepository: ArtifactFileRepository,
    private val annotationRepository: AnnotationRepository,
) {
    fun findById(id: Long): ArtifactRecord? {
        val artifact = Artifacts.selectAll().where { Artifacts.id eq id }
            .map { it.toRecord() }
            .singleOrNull() ?: return null
        val files = artifactFileRepository.findByArtifactIdOrderBySequence(id)
        return artifact.copy(files = files)
    }

    fun findBySlug(slug: String): ArtifactRecord? {
        val artifact = Artifacts.selectAll().where { Artifacts.slug eq slug }
            .map { it.toRecord() }
            .singleOrNull() ?: return null
        val files = artifactFileRepository.findByArtifactIdOrderBySequence(artifact.id)
        return artifact.copy(files = files)
    }

    fun findAll(pageRequest: PageRequest): Page<ArtifactRecord> {
        val total = Artifacts.selectAll().count()
        val artifacts = Artifacts.selectAll()
            .orderBy(Artifacts.uploadedAt, SortOrder.DESC)
            .limit(pageRequest.size)
            .offset((pageRequest.page * pageRequest.size).toLong())
            .map { it.toRecord() }
        val withFiles = loadFiles(artifacts)
        return Page(withFiles, total, pageRequest.page, pageRequest.size)
    }

    fun findByArtifactType(artifactType: ArtifactType, pageRequest: PageRequest): Page<ArtifactRecord> {
        val query = Artifacts.selectAll().where { Artifacts.artifactType eq artifactType }
        val total = query.count()
        val artifacts = query
            .orderBy(Artifacts.uploadedAt, SortOrder.DESC)
            .limit(pageRequest.size)
            .offset((pageRequest.page * pageRequest.size).toLong())
            .map { it.toRecord() }
        val withFiles = loadFiles(artifacts)
        return Page(withFiles, total, pageRequest.page, pageRequest.size)
    }

    fun findByArtifactTypeAndSlug(artifactType: ArtifactType, slug: String): ArtifactRecord? {
        val artifact = Artifacts.selectAll().where {
            (Artifacts.artifactType eq artifactType) and (Artifacts.slug eq slug)
        }.map { it.toRecord() }.singleOrNull() ?: return null
        val files = artifactFileRepository.findByArtifactIdOrderBySequence(artifact.id)
        return artifact.copy(files = files)
    }

    fun findPhotoByIdWithAnnotations(id: Long): ArtifactRecord? {
        val artifact = Artifacts.selectAll().where { Artifacts.id eq id }
            .map { it.toRecord() }
            .singleOrNull() ?: return null
        val files = artifactFileRepository.findByArtifactIdOrderBySequence(id)
        return artifact.copy(files = files)
        // Annotations are loaded separately when needed via AnnotationRepository
    }

    fun findPhotoBySlugWithAnnotations(slug: String): ArtifactRecord? {
        val artifact = Artifacts.selectAll().where { Artifacts.slug eq slug }
            .map { it.toRecord() }
            .singleOrNull() ?: return null
        val files = artifactFileRepository.findByArtifactIdOrderBySequence(artifact.id)
        return artifact.copy(files = files)
    }

    fun findPhotosByArtifactTypeWithAnnotations(artifactType: ArtifactType, pageRequest: PageRequest): Page<ArtifactRecord> =
        findByArtifactType(artifactType, pageRequest)

    fun create(
        slug: String,
        artifactType: ArtifactType,
        title: String?,
        storagePath: String,
        mimeType: String,
        fileSize: Long,
        originalDateString: String?,
    ): Long =
        Artifacts.insert {
            it[Artifacts.slug] = slug
            it[Artifacts.artifactType] = artifactType
            it[Artifacts.title] = title
            it[Artifacts.storagePath] = storagePath
            it[Artifacts.mimeType] = mimeType
            it[Artifacts.fileSize] = fileSize
            it[Artifacts.originalDateString] = originalDateString
        }[Artifacts.id].value

    fun update(id: Long, title: String?, artifactType: ArtifactType, originalDateString: String?) {
        Artifacts.update({ Artifacts.id eq id }) {
            it[Artifacts.title] = title
            it[Artifacts.artifactType] = artifactType
            it[Artifacts.originalDateString] = originalDateString
            it[updatedAt] = LocalDateTime.now()
        }
    }

    fun delete(id: Long) {
        Artifacts.deleteWhere { Artifacts.id eq id }
    }

    fun count(): Long = Artifacts.selectAll().count()

    private fun loadFiles(artifacts: List<ArtifactRecord>): List<ArtifactRecord> {
        if (artifacts.isEmpty()) return artifacts
        val ids = artifacts.map { it.id }
        val allFiles = ArtifactFiles.selectAll()
            .where { ArtifactFiles.artifactId inList ids }
            .orderBy(ArtifactFiles.fileSequence, SortOrder.ASC)
            .map {
                ArtifactFileRecord(
                    id = it[ArtifactFiles.id].value,
                    artifactId = it[ArtifactFiles.artifactId].value,
                    fileSequence = it[ArtifactFiles.fileSequence],
                    storagePath = it[ArtifactFiles.storagePath],
                    mimeType = it[ArtifactFiles.mimeType],
                    fileSize = it[ArtifactFiles.fileSize],
                    thumbnailPath = it[ArtifactFiles.thumbnailPath],
                    thumbnailSize = it[ArtifactFiles.thumbnailSize],
                    uploadedAt = it[ArtifactFiles.uploadedAt],
                    createdAt = it[ArtifactFiles.createdAt],
                    updatedAt = it[ArtifactFiles.updatedAt],
                )
            }
        val filesByArtifact = allFiles.groupBy { it.artifactId }
        return artifacts.map { it.copy(files = filesByArtifact[it.id] ?: emptyList()) }
    }

    private fun ResultRow.toRecord() = ArtifactRecord(
        id = this[Artifacts.id].value,
        slug = this[Artifacts.slug],
        artifactType = this[Artifacts.artifactType],
        title = this[Artifacts.title],
        storagePath = this[Artifacts.storagePath],
        mimeType = this[Artifacts.mimeType],
        fileSize = this[Artifacts.fileSize],
        uploadedAt = this[Artifacts.uploadedAt],
        originalDateString = this[Artifacts.originalDateString],
        createdAt = this[Artifacts.createdAt],
        updatedAt = this[Artifacts.updatedAt],
    )

}

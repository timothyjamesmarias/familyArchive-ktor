package com.timothymarias.familyarchive.service

import com.timothymarias.familyarchive.repository.CommentaryRecord
import com.timothymarias.familyarchive.repository.CommentaryRepository
import org.jetbrains.exposed.sql.transactions.transaction

class CommentaryService(
    private val commentaryRepository: CommentaryRepository,
) {
    fun findById(id: Long): CommentaryRecord? = transaction { commentaryRepository.findById(id) }
    fun findByArtifactId(artifactId: Long): List<CommentaryRecord> = transaction { commentaryRepository.findByArtifactId(artifactId) }
    fun create(artifactId: Long, text: String, type: String?): Long = transaction { commentaryRepository.create(artifactId, text, type) }
    fun update(id: Long, text: String, type: String?) = transaction { commentaryRepository.update(id, text, type) }
    fun delete(id: Long) = transaction { commentaryRepository.delete(id) }
}

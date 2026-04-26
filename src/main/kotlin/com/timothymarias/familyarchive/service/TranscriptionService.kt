package com.timothymarias.familyarchive.service

import com.timothymarias.familyarchive.repository.TranscriptionRecord
import com.timothymarias.familyarchive.repository.TranscriptionRepository
import org.jetbrains.exposed.sql.transactions.transaction

class TranscriptionService(
    private val transcriptionRepository: TranscriptionRepository,
) {
    fun findById(id: Long): TranscriptionRecord? = transaction { transcriptionRepository.findById(id) }
    fun findByArtifactId(artifactId: Long): TranscriptionRecord? = transaction { transcriptionRepository.findByArtifactId(artifactId) }
    fun create(artifactId: Long, text: String): Long = transaction { transcriptionRepository.create(artifactId, text) }
    fun update(id: Long, text: String) = transaction { transcriptionRepository.update(id, text) }
    fun delete(id: Long) = transaction { transcriptionRepository.delete(id) }
}

package com.timothymarias.familyarchive.service

import com.timothymarias.familyarchive.repository.TranslationRecord
import com.timothymarias.familyarchive.repository.TranslationRepository
import org.jetbrains.exposed.sql.transactions.transaction

class TranslationService(
    private val translationRepository: TranslationRepository,
) {
    fun findById(id: Long): TranslationRecord? = transaction { translationRepository.findById(id) }
    fun findByTranscriptionId(transcriptionId: Long): List<TranslationRecord> = transaction { translationRepository.findByTranscriptionId(transcriptionId) }
    fun create(transcriptionId: Long, translatedText: String, targetLanguage: String): Long = transaction { translationRepository.create(transcriptionId, translatedText, targetLanguage) }
    fun update(id: Long, translatedText: String, targetLanguage: String) = transaction { translationRepository.update(id, translatedText, targetLanguage) }
    fun delete(id: Long) = transaction { translationRepository.delete(id) }
}

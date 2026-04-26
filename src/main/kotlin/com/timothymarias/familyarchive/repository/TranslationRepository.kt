package com.timothymarias.familyarchive.repository

import com.timothymarias.familyarchive.database.Translations
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime

data class TranslationRecord(
    val id: Long,
    val transcriptionId: Long,
    val translatedText: String,
    val targetLanguage: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

class TranslationRepository {
    fun findById(id: Long): TranslationRecord? =
        Translations.selectAll().where { Translations.id eq id }
            .map { it.toRecord() }
            .singleOrNull()

    fun findByTranscriptionId(transcriptionId: Long): List<TranslationRecord> =
        Translations.selectAll().where { Translations.transcriptionId eq transcriptionId }
            .map { it.toRecord() }

    fun create(transcriptionId: Long, translatedText: String, targetLanguage: String): Long =
        Translations.insert {
            it[Translations.transcriptionId] = transcriptionId
            it[Translations.translatedText] = translatedText
            it[Translations.targetLanguage] = targetLanguage
        }[Translations.id].value

    fun update(id: Long, translatedText: String, targetLanguage: String) {
        Translations.update({ Translations.id eq id }) {
            it[Translations.translatedText] = translatedText
            it[Translations.targetLanguage] = targetLanguage
            it[updatedAt] = LocalDateTime.now()
        }
    }

    fun delete(id: Long) {
        Translations.deleteWhere { Translations.id eq id }
    }

    private fun ResultRow.toRecord() = TranslationRecord(
        id = this[Translations.id].value,
        transcriptionId = this[Translations.transcriptionId].value,
        translatedText = this[Translations.translatedText],
        targetLanguage = this[Translations.targetLanguage],
        createdAt = this[Translations.createdAt],
        updatedAt = this[Translations.updatedAt],
    )
}

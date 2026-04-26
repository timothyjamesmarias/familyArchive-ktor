package com.timothymarias.familyarchive.repository

import com.timothymarias.familyarchive.database.IndividualEvents
import com.timothymarias.familyarchive.model.EventType
import kotlinx.serialization.json.JsonElement
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime

data class IndividualEventRecord(
    val id: Long,
    val individualId: Long,
    val eventType: EventType,
    val dateString: String?,
    val dateParsed: LocalDateTime?,
    val placeId: Long?,
    val description: String?,
    val gedcomRawData: JsonElement?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

class IndividualEventRepository {
    fun findById(id: Long): IndividualEventRecord? =
        IndividualEvents.selectAll().where { IndividualEvents.id eq id }
            .map { it.toRecord() }
            .singleOrNull()

    fun findByIndividualId(individualId: Long): List<IndividualEventRecord> =
        IndividualEvents.selectAll().where { IndividualEvents.individualId eq individualId }
            .map { it.toRecord() }

    fun findByIndividualIdAndEventType(individualId: Long, eventType: EventType): List<IndividualEventRecord> =
        IndividualEvents.selectAll().where {
            (IndividualEvents.individualId eq individualId) and (IndividualEvents.eventType eq eventType)
        }.map { it.toRecord() }

    fun findByPlaceId(placeId: Long): List<IndividualEventRecord> =
        IndividualEvents.selectAll().where { IndividualEvents.placeId eq placeId }
            .map { it.toRecord() }

    fun findByIndividualIdOrderByDate(individualId: Long): List<IndividualEventRecord> =
        IndividualEvents.selectAll().where { IndividualEvents.individualId eq individualId }
            .orderBy(IndividualEvents.dateParsed to SortOrder.ASC_NULLS_LAST, IndividualEvents.dateString to SortOrder.ASC)
            .map { it.toRecord() }

    fun create(
        individualId: Long,
        eventType: EventType,
        dateString: String?,
        dateParsed: LocalDateTime?,
        placeId: Long?,
        description: String?,
        gedcomRawData: JsonElement?,
    ): Long =
        IndividualEvents.insert {
            it[IndividualEvents.individualId] = individualId
            it[IndividualEvents.eventType] = eventType
            it[IndividualEvents.dateString] = dateString
            it[IndividualEvents.dateParsed] = dateParsed
            it[IndividualEvents.placeId] = placeId
            it[IndividualEvents.description] = description
            it[IndividualEvents.gedcomRawData] = gedcomRawData
        }[IndividualEvents.id].value

    fun update(id: Long, eventType: EventType, dateString: String?, dateParsed: LocalDateTime?, placeId: Long?, description: String?) {
        IndividualEvents.update({ IndividualEvents.id eq id }) {
            it[IndividualEvents.eventType] = eventType
            it[IndividualEvents.dateString] = dateString
            it[IndividualEvents.dateParsed] = dateParsed
            it[IndividualEvents.placeId] = placeId
            it[IndividualEvents.description] = description
            it[updatedAt] = LocalDateTime.now()
        }
    }

    fun delete(id: Long) {
        IndividualEvents.deleteWhere { IndividualEvents.id eq id }
    }

    fun deleteByIndividualId(individualId: Long) {
        IndividualEvents.deleteWhere { IndividualEvents.individualId eq individualId }
    }

    private fun ResultRow.toRecord() = IndividualEventRecord(
        id = this[IndividualEvents.id].value,
        individualId = this[IndividualEvents.individualId].value,
        eventType = this[IndividualEvents.eventType],
        dateString = this[IndividualEvents.dateString],
        dateParsed = this[IndividualEvents.dateParsed],
        placeId = this[IndividualEvents.placeId]?.value,
        description = this[IndividualEvents.description],
        gedcomRawData = this[IndividualEvents.gedcomRawData],
        createdAt = this[IndividualEvents.createdAt],
        updatedAt = this[IndividualEvents.updatedAt],
    )
}

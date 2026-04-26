package com.timothymarias.familyarchive.repository

import com.timothymarias.familyarchive.database.Families
import com.timothymarias.familyarchive.database.FamilyMembers
import com.timothymarias.familyarchive.database.Individuals
import com.timothymarias.familyarchive.database.Places
import kotlinx.serialization.json.JsonElement
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime

data class FamilyRecord(
    val id: Long,
    val gedcomId: String?,
    val marriageDateString: String?,
    val marriageDateParsed: LocalDateTime?,
    val marriagePlaceId: Long?,
    val divorceDateString: String?,
    val divorceDateParsed: LocalDateTime?,
    val gedcomRawData: JsonElement?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val lastImportedAt: LocalDateTime?,
    val deletedAt: LocalDateTime?,
)

class FamilyRepository {
    fun findById(id: Long): FamilyRecord? =
        Families.selectAll().where { Families.deletedAt.isNull() }.where { Families.id eq id }
            .map { it.toRecord() }
            .singleOrNull()

    fun findByGedcomId(gedcomId: String): FamilyRecord? =
        Families.selectAll().where { Families.deletedAt.isNull() }.where { Families.gedcomId eq gedcomId }
            .map { it.toRecord() }
            .singleOrNull()

    fun findAll(pageRequest: PageRequest): Page<FamilyRecord> {
        val total = Families.selectAll().where { Families.deletedAt.isNull() }.count()
        val content = Families.selectAll().where { Families.deletedAt.isNull() }
            .orderBy(Families.createdAt, SortOrder.DESC)
            .limit(pageRequest.size)
            .offset((pageRequest.page * pageRequest.size).toLong())
            .map { it.toRecord() }
        return Page(content, total, pageRequest.page, pageRequest.size)
    }

    fun searchByGedcomIdOrMembersOrPlace(search: String, pageRequest: PageRequest): Page<FamilyRecord> {
        val pattern = "%${search.lowercase()}%"
        // Use a subquery approach: find family IDs that match
        val matchingFamilyIds = mutableSetOf<Long>()

        // Match by gedcom_id
        Families.selectAll().where {
            (Families.deletedAt.isNull()) and (Families.gedcomId.lowerCase() like pattern)
        }.forEach { matchingFamilyIds.add(it[Families.id].value) }

        // Match by member names
        FamilyMembers.innerJoin(Individuals)
            .selectAll().where {
                (FamilyMembers.deletedAt.isNull()) and (Individuals.deletedAt.isNull()) and
                    ((Individuals.givenName.lowerCase() like pattern) or (Individuals.surname.lowerCase() like pattern))
            }.forEach { matchingFamilyIds.add(it[FamilyMembers.familyId].value) }

        // Match by place name
        Families.innerJoin(Places)
            .selectAll().where {
                (Families.deletedAt.isNull()) and (Places.name.lowerCase() like pattern)
            }.forEach { matchingFamilyIds.add(it[Families.id].value) }

        if (matchingFamilyIds.isEmpty()) {
            return Page(emptyList(), 0, pageRequest.page, pageRequest.size)
        }

        val total = matchingFamilyIds.size.toLong()
        val content = Families.selectAll().where {
            (Families.id inList matchingFamilyIds) and (Families.deletedAt.isNull())
        }
            .orderBy(Families.createdAt, SortOrder.DESC)
            .limit(pageRequest.size)
            .offset((pageRequest.page * pageRequest.size).toLong())
            .map { it.toRecord() }

        return Page(content, total, pageRequest.page, pageRequest.size)
    }

    fun findMaxGedcomIdNumber(): Int? {
        var result: Int? = null
        TransactionManager.current().exec(
            """
            SELECT MAX(CAST(SUBSTRING(gedcom_id FROM '@F([0-9]+)@') AS INTEGER))
            FROM families
            WHERE gedcom_id ~ '@F[0-9]+@'
            """.trimIndent(),
        ) { rs ->
            if (rs.next()) {
                result = rs.getInt(1)
                if (rs.wasNull()) result = null
            }
        }
        return result
    }

    fun create(
        gedcomId: String?,
        marriageDateString: String?,
        marriageDateParsed: LocalDateTime?,
        marriagePlaceId: Long?,
        divorceDateString: String?,
        divorceDateParsed: LocalDateTime?,
        gedcomRawData: JsonElement?,
    ): Long =
        Families.insert {
            it[Families.gedcomId] = gedcomId
            it[Families.marriageDateString] = marriageDateString
            it[Families.marriageDateParsed] = marriageDateParsed
            it[Families.marriagePlaceId] = marriagePlaceId
            it[Families.divorceDateString] = divorceDateString
            it[Families.divorceDateParsed] = divorceDateParsed
            it[Families.gedcomRawData] = gedcomRawData
        }[Families.id].value

    fun update(
        id: Long,
        marriageDateString: String?,
        marriageDateParsed: LocalDateTime?,
        marriagePlaceId: Long?,
        divorceDateString: String?,
        divorceDateParsed: LocalDateTime?,
    ) {
        Families.update({ Families.id eq id }) {
            it[Families.marriageDateString] = marriageDateString
            it[Families.marriageDateParsed] = marriageDateParsed
            it[Families.marriagePlaceId] = marriagePlaceId
            it[Families.divorceDateString] = divorceDateString
            it[Families.divorceDateParsed] = divorceDateParsed
            it[updatedAt] = LocalDateTime.now()
        }
    }

    fun softDelete(id: Long) {
        Families.update({ Families.id eq id }) {
            it[deletedAt] = LocalDateTime.now()
        }
    }

    fun count(): Long = Families.selectAll().where { Families.deletedAt.isNull() }.count()

    private fun ResultRow.toRecord() = FamilyRecord(
        id = this[Families.id].value,
        gedcomId = this[Families.gedcomId],
        marriageDateString = this[Families.marriageDateString],
        marriageDateParsed = this[Families.marriageDateParsed],
        marriagePlaceId = this[Families.marriagePlaceId]?.value,
        divorceDateString = this[Families.divorceDateString],
        divorceDateParsed = this[Families.divorceDateParsed],
        gedcomRawData = this[Families.gedcomRawData],
        createdAt = this[Families.createdAt],
        updatedAt = this[Families.updatedAt],
        lastImportedAt = this[Families.lastImportedAt],
        deletedAt = this[Families.deletedAt],
    )
}

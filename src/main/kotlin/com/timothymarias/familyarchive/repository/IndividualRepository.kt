package com.timothymarias.familyarchive.repository

import com.timothymarias.familyarchive.database.Individuals
import kotlinx.serialization.json.JsonElement
import org.jetbrains.exposed.sql.LowerCase
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime

data class IndividualRecord(
    val id: Long,
    val gedcomId: String?,
    val givenName: String?,
    val surname: String?,
    val sex: Char?,
    val isTreeRoot: Boolean,
    val gedcomRawData: JsonElement?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val lastImportedAt: LocalDateTime?,
    val deletedAt: LocalDateTime?,
)

class IndividualRepository {
    fun findById(id: Long): IndividualRecord? =
        Individuals.selectAll().where { (Individuals.id eq id) and (Individuals.deletedAt.isNull()) }
            .map { it.toRecord() }
            .singleOrNull()

    fun findByGedcomId(gedcomId: String): IndividualRecord? =
        Individuals.selectAll().where { (Individuals.gedcomId eq gedcomId) and (Individuals.deletedAt.isNull()) }
            .map { it.toRecord() }
            .singleOrNull()

    fun findByIsTreeRoot(isTreeRoot: Boolean): List<IndividualRecord> =
        Individuals.selectAll().where { (Individuals.isTreeRoot eq isTreeRoot) and (Individuals.deletedAt.isNull()) }
            .map { it.toRecord() }

    fun findBySurnameContainingIgnoreCase(surname: String): List<IndividualRecord> =
        Individuals.selectAll().where {
            (Individuals.deletedAt.isNull()) and (Individuals.surname.lowerCase() like "%${surname.lowercase()}%")
        }.map { it.toRecord() }

    fun searchByNameOrGedcomId(search: String, pageRequest: PageRequest): Page<IndividualRecord> {
        val pattern = "%${search.lowercase()}%"
        val query = Individuals.selectAll().where {
            (Individuals.deletedAt.isNull()) and (
                (Individuals.givenName.lowerCase() like pattern) or
                    (Individuals.surname.lowerCase() like pattern) or
                    (Individuals.gedcomId.lowerCase() like pattern)
                )
        }
        val total = query.count()
        val content = query
            .orderBy(Individuals.surname, SortOrder.ASC)
            .limit(pageRequest.size)
            .offset((pageRequest.page * pageRequest.size).toLong())
            .map { it.toRecord() }
        return Page(content, total, pageRequest.page, pageRequest.size)
    }

    fun findAll(pageRequest: PageRequest): Page<IndividualRecord> {
        val baseQuery = Individuals.selectAll().where { Individuals.deletedAt.isNull() }
        val total = baseQuery.count()
        val content = Individuals.selectAll().where { Individuals.deletedAt.isNull() }
            .orderBy(Individuals.surname, SortOrder.ASC)
            .limit(pageRequest.size)
            .offset((pageRequest.page * pageRequest.size).toLong())
            .map { it.toRecord() }
        return Page(content, total, pageRequest.page, pageRequest.size)
    }

    fun findAncestors(personId: Long, maxGenerations: Int): List<IndividualRecord> {
        val results = mutableListOf<IndividualRecord>()
        TransactionManager.current().exec(
            """
            WITH RECURSIVE ancestors AS (
                SELECT i.id, i.gedcom_id, i.given_name, i.surname, i.sex, i.is_tree_root,
                       i.gedcom_raw_data, i.created_at, i.updated_at, i.last_imported_at, i.deleted_at,
                       0 as generation
                FROM individuals i
                WHERE i.id = $personId
                AND i.deleted_at IS NULL

                UNION ALL

                SELECT i.id, i.gedcom_id, i.given_name, i.surname, i.sex, i.is_tree_root,
                       i.gedcom_raw_data, i.created_at, i.updated_at, i.last_imported_at, i.deleted_at,
                       a.generation + 1
                FROM individuals i
                INNER JOIN family_members fm ON fm.individual_id = i.id AND fm.deleted_at IS NULL
                INNER JOIN family_members fm_child ON fm_child.family_id = fm.family_id AND fm_child.deleted_at IS NULL
                INNER JOIN ancestors a ON a.id = fm_child.individual_id
                WHERE fm.role IN ('FATHER', 'MOTHER')
                AND fm_child.role = 'CHILD'
                AND i.deleted_at IS NULL
                AND a.generation < $maxGenerations
            )
            SELECT DISTINCT id, gedcom_id, given_name, surname, sex, is_tree_root,
                   gedcom_raw_data, created_at, updated_at, last_imported_at, deleted_at
            FROM ancestors
            WHERE deleted_at IS NULL
            ORDER BY id
            """.trimIndent(),
        ) { rs ->
            while (rs.next()) {
                results.add(rs.toIndividualRecord())
            }
        }
        return results
    }

    fun findDescendants(personId: Long, maxGenerations: Int): List<IndividualRecord> {
        val results = mutableListOf<IndividualRecord>()
        TransactionManager.current().exec(
            """
            WITH RECURSIVE descendants AS (
                SELECT i.id, i.gedcom_id, i.given_name, i.surname, i.sex, i.is_tree_root,
                       i.gedcom_raw_data, i.created_at, i.updated_at, i.last_imported_at, i.deleted_at,
                       0 as generation
                FROM individuals i
                WHERE i.id = $personId
                AND i.deleted_at IS NULL

                UNION ALL

                SELECT i.id, i.gedcom_id, i.given_name, i.surname, i.sex, i.is_tree_root,
                       i.gedcom_raw_data, i.created_at, i.updated_at, i.last_imported_at, i.deleted_at,
                       d.generation + 1
                FROM individuals i
                INNER JOIN family_members fm ON fm.individual_id = i.id AND fm.deleted_at IS NULL
                INNER JOIN family_members fm_parent ON fm_parent.family_id = fm.family_id AND fm_parent.deleted_at IS NULL
                INNER JOIN descendants d ON d.id = fm_parent.individual_id
                WHERE fm.role = 'CHILD'
                AND fm_parent.role IN ('FATHER', 'MOTHER')
                AND i.deleted_at IS NULL
                AND d.generation < $maxGenerations
            )
            SELECT DISTINCT id, gedcom_id, given_name, surname, sex, is_tree_root,
                   gedcom_raw_data, created_at, updated_at, last_imported_at, deleted_at
            FROM descendants
            WHERE deleted_at IS NULL
            ORDER BY id
            """.trimIndent(),
        ) { rs ->
            while (rs.next()) {
                results.add(rs.toIndividualRecord())
            }
        }
        return results
    }

    fun findSiblings(personId: Long): List<IndividualRecord> {
        val results = mutableListOf<IndividualRecord>()
        TransactionManager.current().exec(
            """
            SELECT DISTINCT i.id, i.gedcom_id, i.given_name, i.surname, i.sex, i.is_tree_root,
                   i.gedcom_raw_data, i.created_at, i.updated_at, i.last_imported_at, i.deleted_at
            FROM individuals i
            INNER JOIN family_members fm ON fm.individual_id = i.id AND fm.deleted_at IS NULL
            WHERE fm.family_id IN (
                SELECT fm2.family_id FROM family_members fm2
                WHERE fm2.individual_id = $personId
                AND fm2.role = 'CHILD'
                AND fm2.deleted_at IS NULL
            )
            AND fm.role = 'CHILD'
            AND i.id != $personId
            AND i.deleted_at IS NULL
            """.trimIndent(),
        ) { rs ->
            while (rs.next()) {
                results.add(rs.toIndividualRecord())
            }
        }
        return results
    }

    fun findMostRecentGenerationBySurname(surname: String): List<IndividualRecord> {
        val results = mutableListOf<IndividualRecord>()
        TransactionManager.current().exec(
            """
            SELECT DISTINCT i.id, i.gedcom_id, i.given_name, i.surname, i.sex, i.is_tree_root,
                   i.gedcom_raw_data, i.created_at, i.updated_at, i.last_imported_at, i.deleted_at
            FROM individuals i
            LEFT JOIN family_members fm ON fm.individual_id = i.id AND fm.deleted_at IS NULL
            WHERE i.surname = '$surname'
            AND i.deleted_at IS NULL
            AND NOT EXISTS (
                SELECT 1 FROM family_members fm2
                WHERE fm2.individual_id = i.id
                AND fm2.role IN ('FATHER', 'MOTHER')
                AND fm2.deleted_at IS NULL
            )
            ORDER BY i.id
            """.trimIndent(),
        ) { rs ->
            while (rs.next()) {
                results.add(rs.toIndividualRecord())
            }
        }
        return results
    }

    fun findMaxGedcomIdNumber(): Int? {
        var result: Int? = null
        TransactionManager.current().exec(
            """
            SELECT MAX(CAST(SUBSTRING(gedcom_id FROM '@I([0-9]+)@') AS INTEGER))
            FROM individuals
            WHERE gedcom_id ~ '@I[0-9]+@'
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
        givenName: String?,
        surname: String?,
        sex: Char?,
        isTreeRoot: Boolean,
        gedcomRawData: JsonElement?,
    ): Long =
        Individuals.insert {
            it[Individuals.gedcomId] = gedcomId
            it[Individuals.givenName] = givenName
            it[Individuals.surname] = surname
            it[Individuals.sex] = sex
            it[Individuals.isTreeRoot] = isTreeRoot
            it[Individuals.gedcomRawData] = gedcomRawData
        }[Individuals.id].value

    fun update(
        id: Long,
        gedcomId: String?,
        givenName: String?,
        surname: String?,
        sex: Char?,
        isTreeRoot: Boolean,
        lastImportedAt: LocalDateTime?,
    ) {
        Individuals.update({ Individuals.id eq id }) {
            it[Individuals.gedcomId] = gedcomId
            it[Individuals.givenName] = givenName
            it[Individuals.surname] = surname
            it[Individuals.sex] = sex
            it[Individuals.isTreeRoot] = isTreeRoot
            it[Individuals.lastImportedAt] = lastImportedAt
            it[updatedAt] = LocalDateTime.now()
        }
    }

    fun softDelete(id: Long) {
        Individuals.update({ Individuals.id eq id }) {
            it[deletedAt] = LocalDateTime.now()
        }
    }

    fun count(): Long =
        Individuals.selectAll().where { Individuals.deletedAt.isNull() }.count()

    private fun ResultRow.toRecord() = IndividualRecord(
        id = this[Individuals.id].value,
        gedcomId = this[Individuals.gedcomId],
        givenName = this[Individuals.givenName],
        surname = this[Individuals.surname],
        sex = this[Individuals.sex],
        isTreeRoot = this[Individuals.isTreeRoot],
        gedcomRawData = this[Individuals.gedcomRawData],
        createdAt = this[Individuals.createdAt],
        updatedAt = this[Individuals.updatedAt],
        lastImportedAt = this[Individuals.lastImportedAt],
        deletedAt = this[Individuals.deletedAt],
    )

    private fun java.sql.ResultSet.toIndividualRecord(): IndividualRecord {
        val timestamp = { col: String -> getTimestamp(col)?.toLocalDateTime() }
        return IndividualRecord(
            id = getLong("id"),
            gedcomId = getString("gedcom_id"),
            givenName = getString("given_name"),
            surname = getString("surname"),
            sex = getString("sex")?.firstOrNull(),
            isTreeRoot = getBoolean("is_tree_root"),
            gedcomRawData = null, // Raw SQL doesn't deserialize JSONB automatically
            createdAt = timestamp("created_at")!!,
            updatedAt = timestamp("updated_at")!!,
            lastImportedAt = timestamp("last_imported_at"),
            deletedAt = timestamp("deleted_at"),
        )
    }
}

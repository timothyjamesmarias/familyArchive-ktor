package com.timothymarias.familyarchive.repository

import com.timothymarias.familyarchive.database.Places
import kotlinx.serialization.json.JsonElement
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.math.BigDecimal
import java.time.LocalDateTime

data class PlaceRecord(
    val id: Long,
    val name: String,
    val normalizedName: String?,
    val city: String?,
    val stateProvince: String?,
    val country: String?,
    val latitude: BigDecimal?,
    val longitude: BigDecimal?,
    val gedcomRawData: JsonElement?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

class PlaceRepository {
    fun findById(id: Long): PlaceRecord? =
        Places.selectAll().where { Places.id eq id }
            .map { it.toRecord() }
            .singleOrNull()

    fun findByNormalizedName(normalizedName: String): PlaceRecord? =
        Places.selectAll().where { Places.normalizedName eq normalizedName }
            .map { it.toRecord() }
            .singleOrNull()

    fun findByNameContainingIgnoreCase(name: String): List<PlaceRecord> =
        Places.selectAll().where { Places.name.lowerCase() like "%${name.lowercase()}%" }
            .map { it.toRecord() }

    fun findAll(pageRequest: PageRequest): Page<PlaceRecord> {
        val total = Places.selectAll().count()
        val content = Places.selectAll()
            .orderBy(Places.name, SortOrder.ASC)
            .limit(pageRequest.size)
            .offset((pageRequest.page * pageRequest.size).toLong())
            .map { it.toRecord() }
        return Page(content, total, pageRequest.page, pageRequest.size)
    }

    fun searchByAnyField(search: String, pageRequest: PageRequest): Page<PlaceRecord> {
        val pattern = "%${search.lowercase()}%"
        val query = Places.selectAll().where {
            (Places.name.lowerCase() like pattern) or
                (Places.city.lowerCase() like pattern) or
                (Places.stateProvince.lowerCase() like pattern) or
                (Places.country.lowerCase() like pattern)
        }
        val total = query.count()
        val content = query
            .orderBy(Places.name, SortOrder.ASC)
            .limit(pageRequest.size)
            .offset((pageRequest.page * pageRequest.size).toLong())
            .map { it.toRecord() }
        return Page(content, total, pageRequest.page, pageRequest.size)
    }

    fun create(
        name: String,
        normalizedName: String?,
        city: String?,
        stateProvince: String?,
        country: String?,
        latitude: BigDecimal?,
        longitude: BigDecimal?,
        gedcomRawData: JsonElement?,
    ): Long =
        Places.insert {
            it[Places.name] = name
            it[Places.normalizedName] = normalizedName
            it[Places.city] = city
            it[Places.stateProvince] = stateProvince
            it[Places.country] = country
            it[Places.latitude] = latitude
            it[Places.longitude] = longitude
            it[Places.gedcomRawData] = gedcomRawData
        }[Places.id].value

    fun update(
        id: Long,
        name: String,
        normalizedName: String?,
        city: String?,
        stateProvince: String?,
        country: String?,
        latitude: BigDecimal?,
        longitude: BigDecimal?,
    ) {
        Places.update({ Places.id eq id }) {
            it[Places.name] = name
            it[Places.normalizedName] = normalizedName
            it[Places.city] = city
            it[Places.stateProvince] = stateProvince
            it[Places.country] = country
            it[Places.latitude] = latitude
            it[Places.longitude] = longitude
            it[updatedAt] = LocalDateTime.now()
        }
    }

    fun delete(id: Long) {
        Places.deleteWhere { Places.id eq id }
    }

    private fun ResultRow.toRecord() = PlaceRecord(
        id = this[Places.id].value,
        name = this[Places.name],
        normalizedName = this[Places.normalizedName],
        city = this[Places.city],
        stateProvince = this[Places.stateProvince],
        country = this[Places.country],
        latitude = this[Places.latitude],
        longitude = this[Places.longitude],
        gedcomRawData = this[Places.gedcomRawData],
        createdAt = this[Places.createdAt],
        updatedAt = this[Places.updatedAt],
    )
}

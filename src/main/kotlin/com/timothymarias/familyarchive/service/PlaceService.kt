package com.timothymarias.familyarchive.service

import com.timothymarias.familyarchive.repository.Page
import com.timothymarias.familyarchive.repository.PageRequest
import com.timothymarias.familyarchive.repository.PlaceRecord
import com.timothymarias.familyarchive.repository.PlaceRepository
import kotlinx.serialization.json.JsonElement
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

class PlaceService(
    private val placeRepository: PlaceRepository,
) {
    fun findById(id: Long): PlaceRecord? = transaction { placeRepository.findById(id) }

    fun findByNormalizedName(normalizedName: String): PlaceRecord? = transaction {
        placeRepository.findByNormalizedName(normalizedName)
    }

    fun findAll(pageRequest: PageRequest): Page<PlaceRecord> = transaction {
        placeRepository.findAll(pageRequest)
    }

    fun searchByAnyField(search: String, pageRequest: PageRequest): Page<PlaceRecord> = transaction {
        placeRepository.searchByAnyField(search, pageRequest)
    }

    fun create(
        name: String,
        normalizedName: String?,
        city: String?,
        stateProvince: String?,
        country: String?,
        latitude: BigDecimal?,
        longitude: BigDecimal?,
        gedcomRawData: JsonElement? = null,
    ): Long = transaction {
        placeRepository.create(name, normalizedName, city, stateProvince, country, latitude, longitude, gedcomRawData)
    }

    fun update(
        id: Long,
        name: String,
        normalizedName: String?,
        city: String?,
        stateProvince: String?,
        country: String?,
        latitude: BigDecimal?,
        longitude: BigDecimal?,
    ) = transaction {
        placeRepository.update(id, name, normalizedName, city, stateProvince, country, latitude, longitude)
    }

    fun delete(id: Long) = transaction { placeRepository.delete(id) }
}

package com.timothymarias.familyarchive.service

import com.timothymarias.familyarchive.repository.FamilyRecord
import com.timothymarias.familyarchive.repository.FamilyRepository
import com.timothymarias.familyarchive.repository.Page
import com.timothymarias.familyarchive.repository.PageRequest
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class FamilyService(
    private val familyRepository: FamilyRepository,
) {
    fun findAll(pageRequest: PageRequest): Page<FamilyRecord> = transaction { familyRepository.findAll(pageRequest) }
    fun findById(id: Long): FamilyRecord? = transaction { familyRepository.findById(id) }
    fun findByGedcomId(gedcomId: String): FamilyRecord? = transaction { familyRepository.findByGedcomId(gedcomId) }

    fun searchByGedcomIdOrMembersOrPlace(search: String, pageRequest: PageRequest): Page<FamilyRecord> = transaction {
        familyRepository.searchByGedcomIdOrMembersOrPlace(search, pageRequest)
    }

    fun create(
        gedcomId: String? = null,
        marriageDateString: String? = null,
        marriageDateParsed: LocalDateTime? = null,
        marriagePlaceId: Long? = null,
        divorceDateString: String? = null,
        divorceDateParsed: LocalDateTime? = null,
    ): Long = transaction {
        familyRepository.create(gedcomId, marriageDateString, marriageDateParsed, marriagePlaceId, divorceDateString, divorceDateParsed, null)
    }

    fun update(
        id: Long,
        marriageDateString: String?,
        marriageDateParsed: LocalDateTime?,
        marriagePlaceId: Long?,
        divorceDateString: String?,
        divorceDateParsed: LocalDateTime?,
    ) = transaction {
        familyRepository.update(id, marriageDateString, marriageDateParsed, marriagePlaceId, divorceDateString, divorceDateParsed)
    }

    fun delete(id: Long) = transaction { familyRepository.softDelete(id) }
    fun count(): Long = transaction { familyRepository.count() }
}

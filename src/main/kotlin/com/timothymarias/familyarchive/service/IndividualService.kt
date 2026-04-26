package com.timothymarias.familyarchive.service

import com.timothymarias.familyarchive.model.EventType
import com.timothymarias.familyarchive.model.FamilyRole
import com.timothymarias.familyarchive.repository.FamilyMemberRepository
import com.timothymarias.familyarchive.repository.IndividualEventRepository
import com.timothymarias.familyarchive.repository.IndividualRecord
import com.timothymarias.familyarchive.repository.IndividualRepository
import com.timothymarias.familyarchive.repository.Page
import com.timothymarias.familyarchive.repository.PageRequest
import com.timothymarias.familyarchive.repository.PlaceRepository
import org.jetbrains.exposed.sql.transactions.transaction

data class ValidationResult(val valid: Boolean, val reason: String?) {
    companion object {
        fun success() = ValidationResult(true, null)
        fun failure(reason: String) = ValidationResult(false, reason)
    }
}

data class IndividualRequest(
    val gedcomId: String? = null,
    val givenName: String? = null,
    val surname: String? = null,
    val sex: String? = null,
    val birthDate: String? = null,
    val birthPlace: String? = null,
    val deathDate: String? = null,
    val deathPlace: String? = null,
) {
    fun getValidationError(): String? {
        if (givenName.isNullOrBlank() && surname.isNullOrBlank()) {
            return "At least one of givenName or surname is required"
        }
        return null
    }
}

class IndividualService(
    private val individualRepository: IndividualRepository,
    private val familyMemberRepository: FamilyMemberRepository,
    private val individualEventRepository: IndividualEventRepository,
    private val placeRepository: PlaceRepository,
) {
    fun findAll(pageRequest: PageRequest): Page<IndividualRecord> = transaction { individualRepository.findAll(pageRequest) }
    fun findById(id: Long): IndividualRecord? = transaction { individualRepository.findById(id) }
    fun findByGedcomId(gedcomId: String): IndividualRecord? = transaction { individualRepository.findByGedcomId(gedcomId) }

    fun searchByNameOrGedcomId(query: String, pageRequest: PageRequest): Page<IndividualRecord> = transaction {
        individualRepository.searchByNameOrGedcomId(query, pageRequest)
    }

    fun findBySurname(surname: String): List<IndividualRecord> = transaction {
        individualRepository.findBySurnameContainingIgnoreCase(surname)
    }

    fun createFromRequest(request: IndividualRequest): Long {
        val validationError = request.getValidationError()
        if (validationError != null) throw IllegalArgumentException(validationError)

        return transaction {
            val gedcomId = request.gedcomId ?: generateGedcomId()

            if (individualRepository.findByGedcomId(gedcomId) != null) {
                throw IllegalArgumentException("Individual with GEDCOM ID '$gedcomId' already exists")
            }

            val id = individualRepository.create(
                gedcomId = gedcomId,
                givenName = request.givenName,
                surname = request.surname,
                sex = request.sex?.firstOrNull(),
                isTreeRoot = false,
                gedcomRawData = null,
            )

            if (request.birthDate != null || request.birthPlace != null) {
                val placeId = findOrCreatePlace(request.birthPlace)
                individualEventRepository.create(id, EventType.BIRTH, request.birthDate, null, placeId, null, null)
            }

            if (request.deathDate != null || request.deathPlace != null) {
                val placeId = findOrCreatePlace(request.deathPlace)
                individualEventRepository.create(id, EventType.DEATH, request.deathDate, null, placeId, null, null)
            }

            id
        }
    }

    fun updateFromRequest(id: Long, request: IndividualRequest) {
        val validationError = request.getValidationError()
        if (validationError != null) throw IllegalArgumentException(validationError)

        transaction {
            val existing = individualRepository.findById(id)
                ?: throw IllegalArgumentException("Individual with ID $id not found")

            individualRepository.update(
                id = id,
                gedcomId = existing.gedcomId,
                givenName = request.givenName,
                surname = request.surname,
                sex = request.sex?.firstOrNull(),
                isTreeRoot = existing.isTreeRoot,
                lastImportedAt = existing.lastImportedAt,
            )

            updateOrCreateEvent(id, EventType.BIRTH, request.birthDate, request.birthPlace)
            updateOrCreateEvent(id, EventType.DEATH, request.deathDate, request.deathPlace)
        }
    }

    private fun updateOrCreateEvent(individualId: Long, eventType: EventType, dateString: String?, placeName: String?) {
        val existingEvents = individualEventRepository.findByIndividualIdAndEventType(individualId, eventType)

        if (dateString == null && placeName == null) {
            existingEvents.forEach { individualEventRepository.delete(it.id) }
        } else {
            val placeId = findOrCreatePlace(placeName)
            if (existingEvents.isEmpty()) {
                individualEventRepository.create(individualId, eventType, dateString, null, placeId, null, null)
            } else {
                val event = existingEvents.first()
                individualEventRepository.update(event.id, eventType, dateString, event.dateParsed, placeId, event.description)
                existingEvents.drop(1).forEach { individualEventRepository.delete(it.id) }
            }
        }
    }

    private fun findOrCreatePlace(placeName: String?): Long? {
        if (placeName.isNullOrBlank()) return null
        val normalizedName = placeName.trim()
        val existing = placeRepository.findByNormalizedName(normalizedName)
        if (existing != null) return existing.id
        return placeRepository.create(normalizedName, normalizedName, null, null, null, null, null, null)
    }

    fun softDelete(id: Long): Boolean {
        val validation = canDelete(id)
        if (!validation.valid) {
            throw IllegalStateException(validation.reason ?: "Cannot delete individual")
        }
        return transaction {
            individualRepository.softDelete(id)
            true
        }
    }

    fun canDelete(id: Long): ValidationResult = transaction {
        individualRepository.findById(id)
            ?: return@transaction ValidationResult.failure("Individual not found")

        val spouseFamilies = familyMemberRepository.findSpouseFamilies(id)
        for (membership in spouseFamilies) {
            val familyMembers = familyMemberRepository.findByFamilyId(membership.familyId)
            val childCount = familyMembers.count { it.role == FamilyRole.CHILD }
            if (childCount > 0) {
                return@transaction ValidationResult.failure(
                    "Cannot delete: individual has $childCount child${if (childCount > 1) "ren" else ""}",
                )
            }
        }

        val descendants = individualRepository.findDescendants(id, 10).filter { it.id != id }
        if (descendants.isNotEmpty()) {
            return@transaction ValidationResult.failure(
                "Cannot delete: individual has ${descendants.size} descendant(s)",
            )
        }

        ValidationResult.success()
    }

    fun generateGedcomId(): String = transaction {
        val maxId = individualRepository.findMaxGedcomIdNumber() ?: 0
        "@I${maxId + 1}@"
    }

    fun count(): Long = transaction { individualRepository.count() }
}

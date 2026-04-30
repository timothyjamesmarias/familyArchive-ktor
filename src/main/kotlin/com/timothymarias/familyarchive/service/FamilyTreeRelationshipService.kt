package com.timothymarias.familyarchive.service

import com.timothymarias.familyarchive.model.FamilyRole
import com.timothymarias.familyarchive.repository.FamilyMemberRepository
import com.timothymarias.familyarchive.repository.FamilyRepository
import com.timothymarias.familyarchive.repository.IndividualRecord
import com.timothymarias.familyarchive.repository.IndividualRepository
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.serialization.Serializable

@Serializable
data class AddChildRequest(
    val parentId: Long,
    val parentFamilyId: Long? = null,
    val childData: IndividualRequest,
)

@Serializable
data class AddSpouseRequest(
    val personId: Long,
    val spouseData: IndividualRequest,
    val marriageData: MarriageData? = null,
)

@Serializable
data class MarriageData(
    val marriageDateString: String? = null,
    val divorceDateString: String? = null,
)

@Serializable
data class AddParentRequest(
    val childId: Long,
    val parentData: IndividualRequest,
    val role: String,
)

@Serializable
data class LinkExistingParentRequest(
    val childId: Long,
    val existingParentId: Long,
    val role: String,
)

@Serializable
data class AddSiblingRequest(
    val personId: Long,
    val siblingData: IndividualRequest,
)

class FamilyTreeRelationshipService(
    private val individualRepository: IndividualRepository,
    private val familyRepository: FamilyRepository,
    private val familyMemberRepository: FamilyMemberRepository,
    private val familyTreeService: FamilyTreeService,
) {
    fun addChild(request: AddChildRequest): FamilyTreeResponse = transaction {
        val parent = individualRepository.findById(request.parentId)
            ?: throw IllegalArgumentException("Parent with ID ${request.parentId} not found")

        val validationError = request.childData.getValidationError()
        if (validationError != null) throw IllegalArgumentException(validationError)

        val childId = createIndividualFromRequest(request.childData)

        val familyId = if (request.parentFamilyId != null) {
            familyRepository.findById(request.parentFamilyId)?.id
                ?: throw IllegalArgumentException("Family with ID ${request.parentFamilyId} not found")
        } else {
            val newFamilyId = familyRepository.create(generateFamilyGedcomId(), null, null, null, null, null, null)
            familyMemberRepository.create(newFamilyId, parent.id, determineParentRole(parent))
            newFamilyId
        }

        val childOrder = getNextChildOrder(familyId)
        familyMemberRepository.create(familyId, childId, FamilyRole.CHILD, childOrder)

        buildTreeResponseForIndividuals(listOf(parent.id, childId))
    }

    fun addSpouse(request: AddSpouseRequest): FamilyTreeResponse = transaction {
        individualRepository.findById(request.personId)
            ?: throw IllegalArgumentException("Person with ID ${request.personId} not found")

        val validationError = request.spouseData.getValidationError()
        if (validationError != null) throw IllegalArgumentException(validationError)

        val spouseId = createIndividualFromRequest(request.spouseData)
        val person = individualRepository.findById(request.personId)!!
        val spouse = individualRepository.findById(spouseId)!!

        val familyId = familyRepository.create(
            gedcomId = generateFamilyGedcomId(),
            marriageDateString = request.marriageData?.marriageDateString,
            marriageDateParsed = null,
            marriagePlaceId = null,
            divorceDateString = request.marriageData?.divorceDateString,
            divorceDateParsed = null,
            gedcomRawData = null,
        )

        familyMemberRepository.create(familyId, person.id, determineParentRole(person))
        familyMemberRepository.create(familyId, spouse.id, determineParentRole(spouse))

        buildTreeResponseForIndividuals(listOf(person.id, spouseId))
    }

    fun addParent(request: AddParentRequest): FamilyTreeResponse = transaction {
        val child = individualRepository.findById(request.childId)
            ?: throw IllegalArgumentException("Child with ID ${request.childId} not found")

        val validationError = request.parentData.getValidationError()
        if (validationError != null) throw IllegalArgumentException(validationError)

        val role = try {
            FamilyRole.valueOf(request.role)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid role: ${request.role}")
        }
        if (role != FamilyRole.FATHER && role != FamilyRole.MOTHER) {
            throw IllegalArgumentException("Role must be FATHER or MOTHER")
        }

        val parentId = createIndividualFromRequest(request.parentData)

        val childFamilies = familyMemberRepository.findByIndividualIdAndRole(child.id, FamilyRole.CHILD)
        val familyId = if (childFamilies.isEmpty()) {
            val newFamilyId = familyRepository.create(generateFamilyGedcomId(), null, null, null, null, null, null)
            familyMemberRepository.create(newFamilyId, child.id, FamilyRole.CHILD)
            newFamilyId
        } else {
            val existingFamilyId = childFamilies.first().familyId
            val existingParents = familyMemberRepository.findByFamilyId(existingFamilyId)
                .filter { it.role == FamilyRole.FATHER || it.role == FamilyRole.MOTHER }

            if (existingParents.size >= 2) throw IllegalStateException("Family already has 2 parents")
            if (existingParents.any { it.role == role }) throw IllegalStateException("${role.name} already exists in family")
            existingFamilyId
        }

        familyMemberRepository.create(familyId, parentId, role)
        buildTreeResponseForIndividuals(listOf(parentId))
    }

    fun linkExistingParent(request: LinkExistingParentRequest): FamilyTreeResponse = transaction {
        val child = individualRepository.findById(request.childId)
            ?: throw IllegalArgumentException("Child with ID ${request.childId} not found")

        individualRepository.findById(request.existingParentId)
            ?: throw IllegalArgumentException("Parent with ID ${request.existingParentId} not found")

        val role = try {
            FamilyRole.valueOf(request.role)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid role: ${request.role}")
        }
        if (role != FamilyRole.FATHER && role != FamilyRole.MOTHER) {
            throw IllegalArgumentException("Role must be FATHER or MOTHER")
        }

        val childFamilies = familyMemberRepository.findByIndividualIdAndRole(child.id, FamilyRole.CHILD)
        val familyId = if (childFamilies.isEmpty()) {
            val newFamilyId = familyRepository.create(generateFamilyGedcomId(), null, null, null, null, null, null)
            familyMemberRepository.create(newFamilyId, child.id, FamilyRole.CHILD)
            newFamilyId
        } else {
            val existingFamilyId = childFamilies.first().familyId
            val existingParents = familyMemberRepository.findByFamilyId(existingFamilyId)
                .filter { it.role == FamilyRole.FATHER || it.role == FamilyRole.MOTHER }

            if (existingParents.size >= 2) throw IllegalStateException("Family already has 2 parents")
            if (existingParents.any { it.role == role }) throw IllegalStateException("${role.name} already exists in family")
            if (familyMemberRepository.findByFamilyId(existingFamilyId).any { it.individualId == request.existingParentId }) {
                throw IllegalStateException("This person is already a member of the child's family")
            }
            existingFamilyId
        }

        familyMemberRepository.create(familyId, request.existingParentId, role)
        buildTreeResponseForIndividuals(listOf(child.id, request.existingParentId))
    }

    fun addSibling(request: AddSiblingRequest): FamilyTreeResponse = transaction {
        val person = individualRepository.findById(request.personId)
            ?: throw IllegalArgumentException("Person with ID ${request.personId} not found")

        val childFamilies = familyMemberRepository.findByIndividualIdAndRole(person.id, FamilyRole.CHILD)
        if (childFamilies.isEmpty()) throw IllegalStateException("Person has no parent family - cannot add sibling")

        val parentFamilyId = childFamilies.first().familyId

        val validationError = request.siblingData.getValidationError()
        if (validationError != null) throw IllegalArgumentException(validationError)

        val siblingId = createIndividualFromRequest(request.siblingData)
        val childOrder = getNextChildOrder(parentFamilyId)
        familyMemberRepository.create(parentFamilyId, siblingId, FamilyRole.CHILD, childOrder)

        buildTreeResponseForIndividuals(listOf(person.id, siblingId))
    }

    private fun createIndividualFromRequest(request: IndividualRequest): Long {
        val gedcomId = request.gedcomId ?: generateIndividualGedcomId()
        if (individualRepository.findByGedcomId(gedcomId) != null) {
            throw IllegalArgumentException("Individual with GEDCOM ID '$gedcomId' already exists")
        }
        return individualRepository.create(
            gedcomId = gedcomId,
            givenName = request.givenName,
            surname = request.surname,
            sex = request.sex?.firstOrNull(),
            isTreeRoot = false,
            gedcomRawData = null,
        )
    }

    private fun determineParentRole(individual: IndividualRecord): FamilyRole =
        when (individual.sex) {
            'M' -> FamilyRole.FATHER
            'F' -> FamilyRole.MOTHER
            else -> FamilyRole.FATHER
        }

    private fun generateIndividualGedcomId(): String {
        val maxId = individualRepository.findMaxGedcomIdNumber() ?: 0
        return "@I${maxId + 1}@"
    }

    private fun generateFamilyGedcomId(): String {
        val maxId = familyRepository.findMaxGedcomIdNumber() ?: 0
        return "@F${maxId + 1}@"
    }

    private fun getNextChildOrder(familyId: Long): Int {
        val children = familyMemberRepository.findByFamilyIdAndRole(familyId, FamilyRole.CHILD)
        return (children.mapNotNull { it.childOrder }.maxOrNull() ?: -1) + 1
    }

    private fun buildTreeResponseForIndividuals(individualIds: List<Long>): FamilyTreeResponse {
        val allIndividualIds = mutableSetOf<Long>()

        individualIds.forEach { id ->
            allIndividualIds.add(id)
            val memberships = familyMemberRepository.findByIndividualId(id)
            memberships.forEach { membership ->
                familyMemberRepository.findByFamilyId(membership.familyId).forEach { member ->
                    allIndividualIds.add(member.individualId)
                }
            }
        }

        val allIndividuals = allIndividualIds.mapNotNull { individualRepository.findById(it) }

        val families = mutableListOf<FamilyUnit>()
        val processedFamilies = mutableSetOf<Long>()

        allIndividualIds.forEach { individualId ->
            familyMemberRepository.findByIndividualId(individualId).forEach { membership ->
                if (processedFamilies.contains(membership.familyId)) return@forEach
                val allMembers = familyMemberRepository.findByFamilyId(membership.familyId)
                val parents = allMembers
                    .filter { it.role == FamilyRole.FATHER || it.role == FamilyRole.MOTHER }
                    .filter { allIndividualIds.contains(it.individualId) }
                    .map { it.individualId }
                val children = allMembers
                    .filter { it.role == FamilyRole.CHILD }
                    .filter { allIndividualIds.contains(it.individualId) }
                    .map { it.individualId }

                if (parents.isNotEmpty() || children.isNotEmpty()) {
                    families.add(FamilyUnit(membership.familyId, parents, children))
                    processedFamilies.add(membership.familyId)
                }
            }
        }

        return FamilyTreeResponse(
            individuals = allIndividuals.map { individual ->
                familyTreeService.toIndividualResponse(individual)
            },
            families = families,
        )
    }
}

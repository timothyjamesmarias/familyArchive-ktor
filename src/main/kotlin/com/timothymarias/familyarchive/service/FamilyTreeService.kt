package com.timothymarias.familyarchive.service

import com.timothymarias.familyarchive.model.FamilyRole
import com.timothymarias.familyarchive.repository.FamilyMemberRepository
import com.timothymarias.familyarchive.repository.IndividualRecord
import com.timothymarias.familyarchive.repository.IndividualRepository
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class FamilyTreeResponse(
    val individuals: List<IndividualTreeNode>,
    val families: List<FamilyUnit>,
)

@Serializable
data class FamilyUnit(
    val familyId: Long,
    val parentIds: List<Long>,
    val childIds: List<Long>,
)

@Serializable
data class RelationshipMetadata(
    val childFamilyIds: List<Long>,
    val spouseFamilyIds: List<Long>,
    val hasUnloadedAncestors: Boolean,
    val hasUnloadedDescendants: Boolean,
    val hasUnloadedSiblings: Boolean,
)

@Serializable
data class IndividualTreeNode(
    val id: Long,
    val gedcomId: String?,
    val givenName: String?,
    val surname: String?,
    val sex: Char?,
    val isTreeRoot: Boolean,
    val metadata: RelationshipMetadata,
)

class FamilyTreeService(
    private val individualRepository: IndividualRepository,
    private val familyMemberRepository: FamilyMemberRepository,
) {
    companion object {
        const val PRIMARY_SURNAME = "Marias"
    }

    fun getRootIndividuals(): List<IndividualRecord> = transaction {
        individualRepository.findMostRecentGenerationBySurname(PRIMARY_SURNAME)
    }

    fun getInitialTree(): FamilyTreeResponse = transaction {
        val roots = individualRepository.findByIsTreeRoot(true)
        buildTreeResponse(roots.map { it.id }, generationsUp = 1, generationsDown = 0, includeSiblings = true)
    }

    fun expandTree(
        personId: Long,
        generationsUp: Int = 0,
        generationsDown: Int = 0,
        includeSiblings: Boolean = false,
    ): FamilyTreeResponse = transaction {
        buildTreeResponse(listOf(personId), generationsUp, generationsDown, includeSiblings)
    }

    private fun buildTreeResponse(
        rootIds: List<Long>,
        generationsUp: Int,
        generationsDown: Int,
        includeSiblings: Boolean,
    ): FamilyTreeResponse {
        val allIndividuals = mutableMapOf<Long, IndividualRecord>()

        rootIds.forEach { rootId ->
            individualRepository.findById(rootId)?.let { allIndividuals[it.id] = it }

            if (generationsUp > 0) {
                individualRepository.findAncestors(rootId, generationsUp).forEach { allIndividuals[it.id] = it }
            }

            if (generationsDown > 0) {
                individualRepository.findDescendants(rootId, generationsDown).forEach { allIndividuals[it.id] = it }
            }

            if (includeSiblings) {
                individualRepository.findSiblings(rootId).forEach { allIndividuals[it.id] = it }
            }
        }

        // Load spouses of all collected individuals
        allIndividuals.keys.toList().forEach { loadSpouses(it, allIndividuals) }

        // Load siblings when parents are visible
        allIndividuals.keys.toList().forEach { loadSiblingsIfParentsPresent(it, allIndividuals) }

        val allIds = allIndividuals.keys
        val families = buildFamilyUnits(allIds.toList(), allIds)

        val individualsWithMetadata = allIndividuals.values.map { individual ->
            val metadata = computeRelationshipMetadata(individual.id, allIds)
            IndividualTreeNode(
                id = individual.id,
                gedcomId = individual.gedcomId,
                givenName = individual.givenName,
                surname = individual.surname,
                sex = individual.sex,
                isTreeRoot = individual.isTreeRoot,
                metadata = metadata,
            )
        }

        return FamilyTreeResponse(individuals = individualsWithMetadata, families = families)
    }

    private fun loadSpouses(individualId: Long, allIndividuals: MutableMap<Long, IndividualRecord>) {
        val spouseFamilies = familyMemberRepository.findSpouseFamilies(individualId)
        spouseFamilies.forEach { familyMember ->
            val familyMembers = familyMemberRepository.findByFamilyId(familyMember.familyId)
            val spouseIds = familyMembers
                .filter { it.role == FamilyRole.FATHER || it.role == FamilyRole.MOTHER }
                .filter { it.individualId != individualId }
                .map { it.individualId }

            spouseIds.forEach { spouseId ->
                if (!allIndividuals.containsKey(spouseId)) {
                    individualRepository.findById(spouseId)?.let { allIndividuals[spouseId] = it }
                }
            }
        }
    }

    private fun loadSiblingsIfParentsPresent(individualId: Long, allIndividuals: MutableMap<Long, IndividualRecord>) {
        val childFamilies = familyMemberRepository.findChildFamilies(individualId)
        childFamilies.forEach { familyMember ->
            val familyMembers = familyMemberRepository.findByFamilyId(familyMember.familyId)
            val parentIds = familyMembers
                .filter { it.role == FamilyRole.FATHER || it.role == FamilyRole.MOTHER }
                .map { it.individualId }

            val hasParentLoaded = parentIds.any { allIndividuals.containsKey(it) }
            if (hasParentLoaded) {
                val childIds = familyMembers
                    .filter { it.role == FamilyRole.CHILD }
                    .map { it.individualId }

                childIds.forEach { childId ->
                    if (!allIndividuals.containsKey(childId)) {
                        individualRepository.findById(childId)?.let { allIndividuals[childId] = it }
                    }
                }
            }
        }
    }

    private fun buildFamilyUnits(individualIds: List<Long>, allIndividualIds: Set<Long>): List<FamilyUnit> {
        val familyUnits = mutableListOf<FamilyUnit>()
        val processedFamilies = mutableSetOf<Long>()

        individualIds.forEach { individualId ->
            val memberships = familyMemberRepository.findByIndividualId(individualId)
            memberships.forEach { membership ->
                val familyId = membership.familyId
                if (processedFamilies.contains(familyId)) return@forEach

                val allMembers = familyMemberRepository.findByFamilyId(familyId)
                val parents = allMembers
                    .filter { it.role == FamilyRole.FATHER || it.role == FamilyRole.MOTHER }
                    .filter { allIndividualIds.contains(it.individualId) }
                    .map { it.individualId }

                val children = allMembers
                    .filter { it.role == FamilyRole.CHILD }
                    .filter { allIndividualIds.contains(it.individualId) }
                    .map { it.individualId }

                if (parents.isNotEmpty() || children.isNotEmpty()) {
                    familyUnits.add(FamilyUnit(familyId, parents, children))
                    processedFamilies.add(familyId)
                }
            }
        }

        return familyUnits
    }

    private fun computeRelationshipMetadata(individualId: Long, loadedIds: Set<Long>): RelationshipMetadata {
        val childFamilies = familyMemberRepository.findChildFamilies(individualId)
        val spouseFamilies = familyMemberRepository.findSpouseFamilies(individualId)

        return RelationshipMetadata(
            childFamilyIds = childFamilies.map { it.familyId },
            spouseFamilyIds = spouseFamilies.map { it.familyId },
            hasUnloadedAncestors = hasUnloadedParents(individualId, loadedIds),
            hasUnloadedDescendants = hasUnloadedChildren(individualId, loadedIds),
            hasUnloadedSiblings = hasUnloadedSiblings(individualId, loadedIds),
        )
    }

    private fun hasUnloadedParents(individualId: Long, loadedIds: Set<Long>): Boolean {
        val childFamilies = familyMemberRepository.findChildFamilies(individualId)
        return childFamilies.any { fm ->
            familyMemberRepository.findByFamilyId(fm.familyId)
                .any { (it.role == FamilyRole.FATHER || it.role == FamilyRole.MOTHER) && !loadedIds.contains(it.individualId) }
        }
    }

    private fun hasUnloadedChildren(individualId: Long, loadedIds: Set<Long>): Boolean {
        val spouseFamilies = familyMemberRepository.findSpouseFamilies(individualId)
        return spouseFamilies.any { fm ->
            familyMemberRepository.findByFamilyId(fm.familyId)
                .any { it.role == FamilyRole.CHILD && !loadedIds.contains(it.individualId) }
        }
    }

    private fun hasUnloadedSiblings(individualId: Long, loadedIds: Set<Long>): Boolean {
        val siblings = individualRepository.findSiblings(individualId)
        return siblings.any { !loadedIds.contains(it.id) }
    }
}

package com.timothymarias.familyarchive.service

import com.timothymarias.familyarchive.model.FamilyRole
import com.timothymarias.familyarchive.repository.FamilyMemberRecord
import com.timothymarias.familyarchive.repository.FamilyMemberRepository
import org.jetbrains.exposed.sql.transactions.transaction

class FamilyMemberService(
    private val familyMemberRepository: FamilyMemberRepository,
) {
    fun findByFamilyId(familyId: Long): List<FamilyMemberRecord> = transaction { familyMemberRepository.findByFamilyId(familyId) }
    fun findByIndividualId(individualId: Long): List<FamilyMemberRecord> = transaction { familyMemberRepository.findByIndividualId(individualId) }
    fun findByFamilyIdAndRole(familyId: Long, role: FamilyRole): List<FamilyMemberRecord> = transaction { familyMemberRepository.findByFamilyIdAndRole(familyId, role) }
    fun findChildFamilies(individualId: Long): List<FamilyMemberRecord> = transaction { familyMemberRepository.findChildFamilies(individualId) }
    fun findSpouseFamilies(individualId: Long): List<FamilyMemberRecord> = transaction { familyMemberRepository.findSpouseFamilies(individualId) }

    fun create(familyId: Long, individualId: Long, role: FamilyRole, childOrder: Int? = null) = transaction {
        familyMemberRepository.create(familyId, individualId, role, childOrder)
    }

    fun softDeleteByFamilyId(familyId: Long) = transaction { familyMemberRepository.softDeleteByFamilyId(familyId) }
    fun hardDeleteByFamilyId(familyId: Long) = transaction { familyMemberRepository.hardDeleteByFamilyId(familyId) }
}

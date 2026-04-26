package com.timothymarias.familyarchive.repository

import com.timothymarias.familyarchive.database.FamilyMembers
import com.timothymarias.familyarchive.model.FamilyRole
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime

data class FamilyMemberRecord(
    val familyId: Long,
    val individualId: Long,
    val role: FamilyRole,
    val childOrder: Int?,
    val createdAt: LocalDateTime,
    val deletedAt: LocalDateTime?,
)

class FamilyMemberRepository {
    fun findByFamilyId(familyId: Long): List<FamilyMemberRecord> =
        FamilyMembers.selectAll().where {
            (FamilyMembers.familyId eq familyId) and (FamilyMembers.deletedAt.isNull())
        }.map { it.toRecord() }

    fun findByIndividualId(individualId: Long): List<FamilyMemberRecord> =
        FamilyMembers.selectAll().where {
            (FamilyMembers.individualId eq individualId) and (FamilyMembers.deletedAt.isNull())
        }.map { it.toRecord() }

    fun findByFamilyIdAndRole(familyId: Long, role: FamilyRole): List<FamilyMemberRecord> =
        FamilyMembers.selectAll().where {
            (FamilyMembers.familyId eq familyId) and (FamilyMembers.role eq role) and (FamilyMembers.deletedAt.isNull())
        }.map { it.toRecord() }

    fun findByIndividualIdAndRole(individualId: Long, role: FamilyRole): List<FamilyMemberRecord> =
        FamilyMembers.selectAll().where {
            (FamilyMembers.individualId eq individualId) and (FamilyMembers.role eq role) and (FamilyMembers.deletedAt.isNull())
        }.map { it.toRecord() }

    fun findChildFamilies(individualId: Long): List<FamilyMemberRecord> =
        FamilyMembers.selectAll().where {
            (FamilyMembers.individualId eq individualId) and (FamilyMembers.role eq FamilyRole.CHILD) and (FamilyMembers.deletedAt.isNull())
        }.map { it.toRecord() }

    fun findSpouseFamilies(individualId: Long): List<FamilyMemberRecord> =
        FamilyMembers.selectAll().where {
            (FamilyMembers.individualId eq individualId) and
                ((FamilyMembers.role eq FamilyRole.FATHER) or (FamilyMembers.role eq FamilyRole.MOTHER)) and
                (FamilyMembers.deletedAt.isNull())
        }.map { it.toRecord() }

    fun create(familyId: Long, individualId: Long, role: FamilyRole, childOrder: Int? = null) {
        FamilyMembers.insert {
            it[FamilyMembers.familyId] = familyId
            it[FamilyMembers.individualId] = individualId
            it[FamilyMembers.role] = role
            it[FamilyMembers.childOrder] = childOrder
        }
    }

    fun softDelete(familyId: Long, individualId: Long, role: FamilyRole) {
        FamilyMembers.update({
            (FamilyMembers.familyId eq familyId) and
                (FamilyMembers.individualId eq individualId) and
                (FamilyMembers.role eq role)
        }) {
            it[deletedAt] = LocalDateTime.now()
        }
    }

    fun softDeleteByFamilyId(familyId: Long) {
        FamilyMembers.update({ FamilyMembers.familyId eq familyId }) {
            it[deletedAt] = LocalDateTime.now()
        }
    }

    fun hardDeleteByFamilyId(familyId: Long) {
        FamilyMembers.deleteWhere { FamilyMembers.familyId eq familyId }
    }

    private fun ResultRow.toRecord() = FamilyMemberRecord(
        familyId = this[FamilyMembers.familyId].value,
        individualId = this[FamilyMembers.individualId].value,
        role = this[FamilyMembers.role],
        childOrder = this[FamilyMembers.childOrder],
        createdAt = this[FamilyMembers.createdAt],
        deletedAt = this[FamilyMembers.deletedAt],
    )
}

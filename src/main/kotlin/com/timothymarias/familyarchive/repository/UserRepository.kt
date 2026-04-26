package com.timothymarias.familyarchive.repository

import com.timothymarias.familyarchive.database.Users
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime

data class UserRecord(
    val id: Long,
    val email: String,
    val password: String,
    val name: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

class UserRepository {
    fun findById(id: Long): UserRecord? =
        Users.selectAll().where { Users.id eq id }
            .map { it.toUserRecord() }
            .singleOrNull()

    fun findByEmail(email: String): UserRecord? =
        Users.selectAll().where { Users.email eq email }
            .map { it.toUserRecord() }
            .singleOrNull()

    fun existsByEmail(email: String): Boolean =
        Users.selectAll().where { Users.email eq email }.count() > 0

    fun findAll(pageRequest: PageRequest): Page<UserRecord> {
        val total = Users.selectAll().count()
        val content = Users.selectAll()
            .orderBy(Users.createdAt, SortOrder.DESC)
            .limit(pageRequest.size)
            .offset((pageRequest.page * pageRequest.size).toLong())
            .map { it.toUserRecord() }
        return Page(content, total, pageRequest.page, pageRequest.size)
    }

    fun count(): Long = Users.selectAll().count()

    fun create(email: String, password: String, name: String): Long =
        Users.insert {
            it[Users.email] = email
            it[Users.password] = password
            it[Users.name] = name
        }[Users.id].value

    fun update(id: Long, email: String, name: String) {
        Users.update({ Users.id eq id }) {
            it[Users.email] = email
            it[Users.name] = name
            it[Users.updatedAt] = LocalDateTime.now()
        }
    }

    fun updatePassword(id: Long, password: String) {
        Users.update({ Users.id eq id }) {
            it[Users.password] = password
            it[Users.updatedAt] = LocalDateTime.now()
        }
    }

    fun delete(id: Long) {
        Users.deleteWhere { Users.id eq id }
    }

    private fun ResultRow.toUserRecord() = UserRecord(
        id = this[Users.id].value,
        email = this[Users.email],
        password = this[Users.password],
        name = this[Users.name],
        createdAt = this[Users.createdAt],
        updatedAt = this[Users.updatedAt],
    )
}

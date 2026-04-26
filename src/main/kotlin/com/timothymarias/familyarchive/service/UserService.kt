package com.timothymarias.familyarchive.service

import at.favre.lib.crypto.bcrypt.BCrypt
import com.timothymarias.familyarchive.repository.Page
import com.timothymarias.familyarchive.repository.PageRequest
import com.timothymarias.familyarchive.repository.UserRecord
import com.timothymarias.familyarchive.repository.UserRepository
import org.jetbrains.exposed.sql.transactions.transaction

class UserService(
    private val userRepository: UserRepository,
) {
    fun findAll(pageRequest: PageRequest): Page<UserRecord> = transaction {
        userRepository.findAll(pageRequest)
    }

    fun findById(id: Long): UserRecord? = transaction {
        userRepository.findById(id)
    }

    fun findByEmail(email: String): UserRecord? = transaction {
        userRepository.findByEmail(email)
    }

    fun existsByEmail(email: String): Boolean = transaction {
        userRepository.existsByEmail(email)
    }

    fun count(): Long = transaction {
        userRepository.count()
    }

    fun create(email: String, password: String, name: String): Long {
        if (existsByEmail(email)) {
            throw IllegalArgumentException("A user with email '$email' already exists")
        }
        val hashedPassword = hashPassword(password)
        return transaction {
            userRepository.create(email, hashedPassword, name)
        }
    }

    fun update(id: Long, email: String, name: String, newPassword: String? = null) {
        transaction {
            val existing = userRepository.findById(id)
                ?: throw IllegalArgumentException("User not found: $id")

            if (existing.email != email && userRepository.existsByEmail(email)) {
                throw IllegalArgumentException("A user with email '$email' already exists")
            }

            userRepository.update(id, email, name)

            if (!newPassword.isNullOrBlank()) {
                userRepository.updatePassword(id, hashPassword(newPassword))
            }
        }
    }

    fun delete(id: Long) = transaction {
        userRepository.delete(id)
    }

    fun verifyPassword(plaintext: String, hashed: String): Boolean =
        BCrypt.verifyer().verify(plaintext.toCharArray(), hashed).verified

    private fun hashPassword(password: String): String =
        BCrypt.withDefaults().hashToString(12, password.toCharArray())
}

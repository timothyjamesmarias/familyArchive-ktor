package com.timothymarias.familyarchive.repository

import com.timothymarias.familyarchive.database.Articles
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime

data class ArticleRecord(
    val id: Long,
    val slug: String,
    val title: String,
    val excerpt: String?,
    val content: String,
    val publishedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    val isPublished: Boolean get() = publishedAt != null && publishedAt.isBefore(LocalDateTime.now())
    val isDraft: Boolean get() = !isPublished
}

class ArticleRepository {
    fun findById(id: Long): ArticleRecord? =
        Articles.selectAll().where { Articles.id eq id }
            .map { it.toRecord() }
            .singleOrNull()

    fun findBySlug(slug: String): ArticleRecord? =
        Articles.selectAll().where { Articles.slug eq slug }
            .map { it.toRecord() }
            .singleOrNull()

    fun findAll(pageRequest: PageRequest): Page<ArticleRecord> {
        val total = Articles.selectAll().count()
        val content = Articles.selectAll()
            .orderBy(Articles.createdAt, SortOrder.DESC)
            .limit(pageRequest.size)
            .offset((pageRequest.page * pageRequest.size).toLong())
            .map { it.toRecord() }
        return Page(content, total, pageRequest.page, pageRequest.size)
    }

    fun findAllPublished(now: LocalDateTime, pageRequest: PageRequest): Page<ArticleRecord> {
        val query = Articles.selectAll().where {
            (Articles.publishedAt.isNotNull()) and (Articles.publishedAt lessEq now)
        }
        val total = query.count()
        val content = query
            .orderBy(Articles.publishedAt, SortOrder.DESC)
            .limit(pageRequest.size)
            .offset((pageRequest.page * pageRequest.size).toLong())
            .map { it.toRecord() }
        return Page(content, total, pageRequest.page, pageRequest.size)
    }

    fun findAllDrafts(now: LocalDateTime, pageRequest: PageRequest): Page<ArticleRecord> {
        val query = Articles.selectAll().where {
            (Articles.publishedAt.isNull()) or (Articles.publishedAt greater now)
        }
        val total = query.count()
        val content = query
            .orderBy(Articles.updatedAt, SortOrder.DESC)
            .limit(pageRequest.size)
            .offset((pageRequest.page * pageRequest.size).toLong())
            .map { it.toRecord() }
        return Page(content, total, pageRequest.page, pageRequest.size)
    }

    fun create(slug: String, title: String, excerpt: String?, content: String, publishedAt: LocalDateTime?): Long =
        Articles.insert {
            it[Articles.slug] = slug
            it[Articles.title] = title
            it[Articles.excerpt] = excerpt
            it[Articles.content] = content
            it[Articles.publishedAt] = publishedAt
        }[Articles.id].value

    fun update(id: Long, slug: String, title: String, excerpt: String?, content: String, publishedAt: LocalDateTime?) {
        Articles.update({ Articles.id eq id }) {
            it[Articles.slug] = slug
            it[Articles.title] = title
            it[Articles.excerpt] = excerpt
            it[Articles.content] = content
            it[Articles.publishedAt] = publishedAt
            it[updatedAt] = LocalDateTime.now()
        }
    }

    fun delete(id: Long) {
        Articles.deleteWhere { Articles.id eq id }
    }

    private fun ResultRow.toRecord() = ArticleRecord(
        id = this[Articles.id].value,
        slug = this[Articles.slug],
        title = this[Articles.title],
        excerpt = this[Articles.excerpt],
        content = this[Articles.content],
        publishedAt = this[Articles.publishedAt],
        createdAt = this[Articles.createdAt],
        updatedAt = this[Articles.updatedAt],
    )
}

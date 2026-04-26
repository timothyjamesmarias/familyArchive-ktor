package com.timothymarias.familyarchive.service

import com.timothymarias.familyarchive.repository.ArticleRecord
import com.timothymarias.familyarchive.repository.ArticleRepository
import com.timothymarias.familyarchive.repository.Page
import com.timothymarias.familyarchive.repository.PageRequest
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class ArticleService(
    private val articleRepository: ArticleRepository,
) {
    fun findAll(pageRequest: PageRequest): Page<ArticleRecord> = transaction {
        articleRepository.findAll(pageRequest)
    }

    fun findById(id: Long): ArticleRecord? = transaction {
        articleRepository.findById(id)
    }

    fun findBySlug(slug: String): ArticleRecord? = transaction {
        articleRepository.findBySlug(slug)
    }

    fun findAllPublished(pageRequest: PageRequest): Page<ArticleRecord> = transaction {
        articleRepository.findAllPublished(LocalDateTime.now(), pageRequest)
    }

    fun findAllDrafts(pageRequest: PageRequest): Page<ArticleRecord> = transaction {
        articleRepository.findAllDrafts(LocalDateTime.now(), pageRequest)
    }

    fun create(slug: String, title: String, excerpt: String?, content: String, publishedAt: LocalDateTime?): Long = transaction {
        articleRepository.create(slug, title, excerpt, content, publishedAt)
    }

    fun update(id: Long, slug: String, title: String, excerpt: String?, content: String, publishedAt: LocalDateTime?) = transaction {
        articleRepository.update(id, slug, title, excerpt, content, publishedAt)
    }

    fun publish(id: Long) = transaction {
        val article = articleRepository.findById(id)
            ?: throw IllegalArgumentException("Article not found: $id")
        articleRepository.update(id, article.slug, article.title, article.excerpt, article.content, LocalDateTime.now())
    }

    fun unpublish(id: Long) = transaction {
        val article = articleRepository.findById(id)
            ?: throw IllegalArgumentException("Article not found: $id")
        articleRepository.update(id, article.slug, article.title, article.excerpt, article.content, null)
    }

    fun delete(id: Long) = transaction {
        articleRepository.delete(id)
    }
}

package com.timothymarias.familyarchive.views.public

import com.timothymarias.familyarchive.components.IconHelpers.backArrowIcon
import com.timothymarias.familyarchive.views.ViewContext
import com.timothymarias.familyarchive.components.pageLayout
import com.timothymarias.familyarchive.views.renderHtml
import com.timothymarias.familyarchive.views.Article
import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.article
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.header
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.unsafe
import com.timothymarias.familyarchive.repository.Page
import java.time.format.DateTimeFormatter

object ArticlesViews {
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy")

    fun index(
        ctx: ViewContext,
        articles: Page<Article>,
    ): String =
        renderHtml {
            pageLayout(
                title = "Articles",
                isDevMode = ctx.isDevMode,
                auth = ctx.auth,
            ) {
                div(classes = "max-w-6xl mx-auto") {
                    h1(classes = "text-4xl font-bold text-gray-900 dark:text-white mb-8") {
                        +"Articles"
                    }

                    // Articles Grid
                    if (articles.content.isEmpty()) {
                        renderEmptyState()
                    } else {
                        renderArticlesGrid(articles.content)
                    }

                    // Simple Pagination
                    renderSimplePagination(articles)
                }
            }
        }

    fun show(
        ctx: ViewContext,
        article: Article,
    ): String =
        renderHtml {
            pageLayout(
                title = article.title,
                isDevMode = ctx.isDevMode,
                auth = ctx.auth,
            ) {
                div(classes = "max-w-4xl mx-auto") {
                    // Back Button
                    a(
                        href = "/articles",
                        classes = "inline-flex items-center text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-200 mb-6",
                    ) {
                        unsafe { +backArrowIcon("w-5 h-5 mr-2") }
                        +"Back to Articles"
                    }

                    // Article
                    article(classes = "bg-white dark:bg-gray-800 rounded-lg shadow-md p-8") {
                        header(classes = "mb-8") {
                            h1(classes = "text-4xl font-bold text-gray-900 dark:text-white mb-4") {
                                +article.title
                            }
                            article.publishedAt?.let { publishedAt ->
                                p(classes = "text-gray-500 dark:text-gray-400") {
                                    +publishedAt.format(DATE_FORMATTER)
                                }
                            }
                        }

                        // Article Content
                        div(
                            classes =
                                "prose prose-lg dark:prose-invert max-w-none " +
                                    "prose-headings:text-gray-900 dark:prose-headings:text-white " +
                                    "prose-p:text-gray-700 dark:prose-p:text-gray-300 " +
                                    "prose-a:text-blue-600 dark:prose-a:text-blue-400 " +
                                    "prose-strong:text-gray-900 dark:prose-strong:text-white " +
                                    "prose-code:text-gray-900 dark:prose-code:text-white " +
                                    "prose-pre:bg-gray-100 dark:prose-pre:bg-gray-900 " +
                                    "prose-blockquote:border-gray-300 dark:prose-blockquote:border-gray-600 " +
                                    "prose-blockquote:text-gray-700 dark:prose-blockquote:text-gray-400 " +
                                    "prose-hr:border-gray-300 dark:prose-hr:border-gray-700 " +
                                    "prose-table:text-gray-700 dark:prose-table:text-gray-300",
                        ) {
                            unsafe {
                                +article.content
                            }
                        }
                    }
                }
            }
        }

    // Helper Functions

    private fun FlowContent.renderArticlesGrid(articles: List<Article>) {
        div(classes = "grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8") {
            articles.forEach { article ->
                renderArticleCard(article)
            }
        }
    }

    private fun FlowContent.renderArticleCard(article: Article) {
        div(
            classes = "bg-white dark:bg-gray-800 rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow",
        ) {
            a(href = "/articles/${article.slug}", classes = "block p-6") {
                h2(classes = "text-xl font-semibold text-gray-900 dark:text-white mb-2") {
                    +article.title
                }
                article.publishedAt?.let { publishedAt ->
                    p(classes = "text-sm text-gray-500 dark:text-gray-400 mb-3") {
                        +publishedAt.format(DATE_FORMATTER)
                    }
                }
                article.excerpt?.let { excerpt ->
                    p(classes = "text-gray-600 dark:text-gray-300 line-clamp-3") {
                        +excerpt
                    }
                }
            }
        }
    }

    private fun FlowContent.renderEmptyState() {
        div(classes = "text-center py-12") {
            p(classes = "text-gray-500 dark:text-gray-400 text-lg") {
                +"No articles published yet."
            }
        }
    }

    private fun FlowContent.renderSimplePagination(articles: Page<Article>) {
        if (articles.totalPages <= 1) return

        div(classes = "flex justify-center items-center space-x-2") {
            renderPreviousButton(articles)
            renderPageIndicator(articles)
            renderNextButton(articles)
        }
    }

    private fun FlowContent.renderPreviousButton(articles: Page<Article>) {
        if (!articles.isFirst) {
            a(
                href = "/articles?page=${articles.number - 1}",
                classes = "px-4 py-2 bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 rounded-md hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors",
            ) {
                +"Previous"
            }
        } else {
            span(classes = "px-4 py-2 bg-gray-100 dark:bg-gray-800 text-gray-400 dark:text-gray-600 rounded-md") {
                +"Previous"
            }
        }
    }

    private fun FlowContent.renderPageIndicator(articles: Page<Article>) {
        span(classes = "px-4 py-2 text-gray-700 dark:text-gray-300") {
            +"Page ${articles.number + 1} of ${articles.totalPages}"
        }
    }

    private fun FlowContent.renderNextButton(articles: Page<Article>) {
        if (!articles.isLast) {
            a(
                href = "/articles?page=${articles.number + 1}",
                classes = "px-4 py-2 bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 rounded-md hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors",
            ) {
                +"Next"
            }
        } else {
            span(classes = "px-4 py-2 bg-gray-100 dark:bg-gray-800 text-gray-400 dark:text-gray-600 rounded-md") {
                +"Next"
            }
        }
    }
}

package com.timothymarias.familyarchive.components

import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.nav
import kotlinx.html.span
import com.timothymarias.familyarchive.repository.Page

/**
 * Reusable pagination components for admin tables.
 * Provides consistent pagination UI across different entity types.
 */
object PaginationComponents {
    /**
     * Renders complete pagination controls with stats and navigation.
     */
    fun FlowContent.renderPagination(
        page: Page<*>,
        baseUrl: String,
        search: String?,
        entityName: String,
    ) {
        if (page.totalPages > 1) {
            div(classes = "mt-6 flex items-center justify-between") {
                renderPaginationStats(page.numberOfElements, page.totalElements, entityName)
                renderPaginationNav(page, baseUrl, search)
            }
        } else if (page.totalElements > 0) {
            div(classes = "mt-4 text-sm text-gray-600 dark:text-gray-400") {
                renderPaginationStats(page.numberOfElements, page.totalElements, entityName)
            }
        }
    }

    /**
     * Renders pagination statistics (e.g., "Showing 20 of 150 total users").
     */
    private fun FlowContent.renderPaginationStats(
        showing: Int,
        total: Long,
        entityName: String,
    ) {
        div(classes = "text-sm text-gray-600 dark:text-gray-400") {
            +"Showing "
            span { +"$showing" }
            +" of "
            span { +"$total" }
            +" total $entityName"
        }
    }

    /**
     * Renders pagination navigation with previous/next buttons and page numbers.
     */
    private fun FlowContent.renderPaginationNav(
        page: Page<*>,
        baseUrl: String,
        search: String?,
    ) {
        nav(classes = "flex items-center gap-2") {
            renderPreviousButton(page, baseUrl, search)
            renderPageNumbers(page, baseUrl, search)
            renderNextButton(page, baseUrl, search)
        }
    }

    /**
     * Renders the "Previous" pagination button.
     */
    private fun FlowContent.renderPreviousButton(
        page: Page<*>,
        baseUrl: String,
        search: String?,
    ) {
        val isEnabled = page.number > 0
        val url = buildPaginationUrl(baseUrl, page.number - 1, search)
        val buttonClasses =
            if (isEnabled) {
                "px-3 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700"
            } else {
                "px-3 py-2 text-sm font-medium text-gray-400 dark:text-gray-600 bg-gray-100 dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-lg cursor-not-allowed"
            }

        if (isEnabled) {
            a(href = url, classes = buttonClasses) { +"Previous" }
        } else {
            span(classes = buttonClasses) { +"Previous" }
        }
    }

    /**
     * Renders page number buttons (showing current page and nearby pages).
     */
    private fun FlowContent.renderPageNumbers(
        page: Page<*>,
        baseUrl: String,
        search: String?,
    ) {
        div(classes = "flex gap-1") {
            for (i in 0 until page.totalPages) {
                if (i in (page.number - 2)..(page.number + 2)) {
                    renderPageNumber(i, page.number, baseUrl, search)
                }
            }
        }
    }

    /**
     * Renders a single page number button.
     */
    private fun FlowContent.renderPageNumber(
        pageNum: Int,
        currentPage: Int,
        baseUrl: String,
        search: String?,
    ) {
        val isCurrent = pageNum == currentPage
        val url = buildPaginationUrl(baseUrl, pageNum, search)

        if (isCurrent) {
            span(classes = "px-3 py-2 text-sm font-medium border rounded-lg bg-blue-600 text-white border-blue-600") {
                +"${pageNum + 1}"
            }
        } else {
            a(
                href = url,
                classes = "px-3 py-2 text-sm font-medium border rounded-lg bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-700",
            ) {
                +"${pageNum + 1}"
            }
        }
    }

    /**
     * Renders the "Next" pagination button.
     */
    private fun FlowContent.renderNextButton(
        page: Page<*>,
        baseUrl: String,
        search: String?,
    ) {
        val isEnabled = page.number < page.totalPages - 1
        val url = buildPaginationUrl(baseUrl, page.number + 1, search)
        val buttonClasses =
            if (isEnabled) {
                "px-3 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700"
            } else {
                "px-3 py-2 text-sm font-medium text-gray-400 dark:text-gray-600 bg-gray-100 dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-lg cursor-not-allowed"
            }

        if (isEnabled) {
            a(href = url, classes = buttonClasses) { +"Next" }
        } else {
            span(classes = buttonClasses) { +"Next" }
        }
    }

    /**
     * Builds a pagination URL with page number and optional search parameter.
     */
    private fun buildPaginationUrl(
        baseUrl: String,
        pageNum: Int,
        search: String?,
    ): String = "$baseUrl?page=$pageNum&size=20&search=${search ?: ""}"
}

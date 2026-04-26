package com.timothymarias.familyarchive.components

import com.timothymarias.familyarchive.components.IconHelpers.chevronRightIcon
import com.timothymarias.familyarchive.components.IconHelpers.documentIcon
import com.timothymarias.familyarchive.views.Artifact
import kotlinx.html.FlowContent
import kotlinx.html.SVG
import kotlinx.html.a
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.h2
import kotlinx.html.h3
import kotlinx.html.img
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.svg
import kotlinx.html.unsafe
import com.timothymarias.familyarchive.repository.Page

/**
 * Artifact list item component - displays a single artifact in a list with thumbnail, title, date, and file count
 */
fun FlowContent.artifactListItem(
    artifact: Artifact,
    typeName: String,
) {
    div(classes = "bg-white dark:bg-gray-800 rounded-lg shadow hover:shadow-lg transition-shadow overflow-hidden") {
        a(
            href = "/$typeName/${artifact.slug}",
            classes = "flex flex-col sm:flex-row gap-4 p-4 hover:bg-gray-50 dark:hover:bg-gray-750 transition-colors",
        ) {
            // Thumbnail
            div(classes = "flex-shrink-0 w-full sm:w-32 h-32 bg-gray-200 dark:bg-gray-700 rounded overflow-hidden") {
                val hasFiles = artifact.files.isNotEmpty()
                val primaryFile = artifact.files.firstOrNull()
                val hasImage =
                    hasFiles &&
                        primaryFile != null &&
                        (
                            primaryFile.thumbnailPath != null ||
                                primaryFile.mimeType?.startsWith("image/") == true
                        )

                if (hasImage && primaryFile != null) {
                    val imagePath = primaryFile.thumbnailPath ?: primaryFile.storagePath
                    img(src = "/uploads/$imagePath", alt = artifact.title ?: "Untitled") {
                        classes = setOf("w-full", "h-full", "object-cover")
                    }
                } else {
                    // Placeholder
                    div(classes = "w-full h-full flex items-center justify-center") {
                        unsafe { +documentIcon("w-12 h-12 text-gray-400 dark:text-gray-500") }
                    }
                }
            }

            // Content
            div(classes = "flex-1 min-w-0") {
                h3(classes = "text-lg font-semibold text-gray-900 dark:text-white truncate mb-1") {
                    +(artifact.title ?: "Untitled $typeName")
                }

                artifact.originalDateString?.let { dateStr ->
                    p(classes = "text-sm text-gray-600 dark:text-gray-400 mb-2") {
                        +dateStr
                    }
                }

                div(classes = "flex items-center gap-4 text-xs text-gray-500 dark:text-gray-500") {
                    val fileCount = artifact.files.size
                    if (fileCount > 0) {
                        span {
                            unsafe { +documentIcon("w-4 h-4 inline-block mr-1") }
                            span {
                                +"$fileCount ${if (fileCount == 1) "file" else "files"}"
                            }
                        }
                    }
                }
            }

            // Chevron
            div(classes = "flex-shrink-0 flex items-center") {
                unsafe { +chevronRightIcon("w-5 h-5 text-gray-400 dark:text-gray-500") }
            }
        }
    }
}

/**
 * Empty state component for artifact lists
 */
fun FlowContent.emptyState(
    title: String,
    description: String,
    icon: SVG.() -> Unit,
) {
    div(classes = "text-center py-12") {
        svg(classes = "w-16 h-16 mx-auto text-gray-400 dark:text-gray-500 mb-4") {
            attributes["fill"] = "none"
            attributes["stroke"] = "currentColor"
            attributes["viewBox"] = "0 0 24 24"
            icon()
        }
        h2(classes = "text-xl font-semibold text-gray-700 dark:text-gray-300 mb-2") {
            +title
        }
        p(classes = "text-gray-500 dark:text-gray-400") {
            +description
        }
    }
}

/**
 * Pagination component for paginated lists
 */
fun FlowContent.pagination(
    page: Page<*>,
    baseUrl: String,
) {
    if (page.totalPages > 1) {
        div(classes = "flex justify-center items-center gap-2 mt-8") {
            // Previous Button
            if (!page.isFirst) {
                a(
                    href = "$baseUrl?page=${page.number - 1}&size=${page.size}",
                    classes = "px-4 py-2 bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 rounded-lg shadow hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors",
                ) {
                    +"Previous"
                }
            } else {
                span(
                    classes = "px-4 py-2 bg-gray-100 dark:bg-gray-900 text-gray-400 dark:text-gray-600 rounded-lg cursor-not-allowed",
                ) {
                    +"Previous"
                }
            }

            // Page Numbers
            div(classes = "flex gap-2") {
                for (i in 0 until page.totalPages) {
                    // Show first page, last page, current page, and pages around current
                    val showPage =
                        i == 0 ||
                            i == page.totalPages - 1 ||
                            (i >= page.number - 2 && i <= page.number + 2)

                    if (showPage) {
                        if (i != page.number) {
                            a(
                                href = "$baseUrl?page=$i&size=${page.size}",
                                classes = "px-4 py-2 bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 rounded-lg shadow hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors",
                            ) {
                                +"${i + 1}"
                            }
                        } else {
                            span(classes = "px-4 py-2 bg-blue-600 text-white rounded-lg shadow font-semibold") {
                                +"${i + 1}"
                            }
                        }
                    }

                    // Show ellipsis
                    if (i == 1 && page.number > 3) {
                        span(classes = "px-2 py-2 text-gray-500 dark:text-gray-400") {
                            +"..."
                        }
                    }
                    if (i == page.totalPages - 2 && page.number < page.totalPages - 4) {
                        span(classes = "px-2 py-2 text-gray-500 dark:text-gray-400") {
                            +"..."
                        }
                    }
                }
            }

            // Next Button
            if (!page.isLast) {
                a(
                    href = "$baseUrl?page=${page.number + 1}&size=${page.size}",
                    classes = "px-4 py-2 bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 rounded-lg shadow hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors",
                ) {
                    +"Next"
                }
            } else {
                span(
                    classes = "px-4 py-2 bg-gray-100 dark:bg-gray-900 text-gray-400 dark:text-gray-600 rounded-lg cursor-not-allowed",
                ) {
                    +"Next"
                }
            }
        }
    }
}

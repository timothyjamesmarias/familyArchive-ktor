package com.timothymarias.familyarchive.views.admin

import com.timothymarias.familyarchive.views.AdminViewContext
import com.timothymarias.familyarchive.components.IconHelpers.infoIcon
import com.timothymarias.familyarchive.components.IconHelpers.photoIcon
import com.timothymarias.familyarchive.components.IconHelpers.refreshIcon
import com.timothymarias.familyarchive.components.adminLayout
import com.timothymarias.familyarchive.views.renderHtml
import com.timothymarias.familyarchive.service.ThumbnailStats
import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.label
import kotlinx.html.option
import kotlinx.html.p
import kotlinx.html.script
import kotlinx.html.select
import kotlinx.html.span
import kotlinx.html.unsafe

/**
 * View functions for admin system utilities pages.
 * Separates HTML rendering logic from controller routing/business logic.
 */
object AdminSystemUtilitiesViews {
    /**
     * Renders the system utilities page with thumbnail management.
     */
    fun utilities(
        ctx: AdminViewContext,
        thumbnailStats: ThumbnailStats,
    ): String =
        renderHtml {
            adminLayout(
                title = "System Utilities - Admin",
                pageTitle = "System Utilities",
                isDevMode = ctx.isDevMode,
                auth = ctx.auth,
                csrf = ctx.csrf,
                successMessage = ctx.successMessage,
                errorMessage = ctx.errorMessage,
            ) {
                // Page Header
                div(classes = "mb-8") {
                    h1(classes = "text-3xl font-bold text-gray-800 dark:text-white") {
                        +"General System Utilities"
                    }
                    p(classes = "text-gray-600 dark:text-gray-400 mt-2") {
                        +"Administrative tools and background job management"
                    }
                }

                // Thumbnail Management Section
                renderThumbnailManagementSection(thumbnailStats)

                // Future System Tools Section (Placeholder)
                div(classes = "bg-white dark:bg-gray-800 rounded-lg shadow p-6") {
                    h2(classes = "text-2xl font-semibold text-gray-800 dark:text-white mb-4") {
                        +"Other System Tools"
                    }
                    p(classes = "text-gray-600 dark:text-gray-400") {
                        +"Additional administrative tools will appear here."
                    }
                }

                // Add JavaScript for thumbnail backfill functionality
                renderThumbnailBackfillScript(thumbnailStats)
            }
        }

    // Helper Functions

    private fun FlowContent.renderThumbnailManagementSection(thumbnailStats: ThumbnailStats) {
        div(classes = "bg-white dark:bg-gray-800 rounded-lg shadow p-6 mb-6") {
            renderThumbnailSectionHeader()
            renderThumbnailStatistics(thumbnailStats)
            renderBackfillControls()
            renderJobRunrDashboardLink()
        }
    }

    private fun FlowContent.renderThumbnailSectionHeader() {
        div(classes = "flex items-center justify-between mb-6") {
            div {
                h2(classes = "text-2xl font-semibold text-gray-800 dark:text-white") {
                    +"Thumbnail Management"
                }
                p(classes = "text-gray-600 dark:text-gray-400 mt-1") {
                    +"Generate thumbnails for artifact images"
                }
            }
            div(classes = "bg-purple-100 dark:bg-purple-900 rounded-full p-3") {
                unsafe { +photoIcon("w-8 h-8 text-purple-600 dark:text-purple-400") }
            }
        }
    }

    private fun FlowContent.renderThumbnailStatistics(thumbnailStats: ThumbnailStats) {
        div(classes = "grid grid-cols-1 md:grid-cols-4 gap-4 mb-6") {
            renderStatCard(
                "Total Artifacts",
                "${thumbnailStats.totalArtifacts}",
                "gray",
            )
            renderStatCard(
                "Image Artifacts",
                "${thumbnailStats.totalImageArtifacts}",
                "blue",
            )
            renderStatCard(
                "With Thumbnails",
                "${thumbnailStats.withThumbnails}",
                "green",
            )
            renderStatCard(
                "Without Thumbnails",
                "${thumbnailStats.withoutThumbnails}",
                "yellow",
            )
        }
    }

    private fun FlowContent.renderStatCard(
        label: String,
        value: String,
        color: String,
    ) {
        val bgClass =
            when (color) {
                "gray" -> "bg-gray-50 dark:bg-gray-700/50"
                "blue" -> "bg-blue-50 dark:bg-blue-900/20"
                "green" -> "bg-green-50 dark:bg-green-900/20"
                "yellow" -> "bg-yellow-50 dark:bg-yellow-900/20"
                else -> "bg-gray-50 dark:bg-gray-700/50"
            }
        val textClass =
            when (color) {
                "gray" -> "text-gray-600 dark:text-gray-400"
                "blue" -> "text-blue-600 dark:text-blue-400"
                "green" -> "text-green-600 dark:text-green-400"
                "yellow" -> "text-yellow-600 dark:text-yellow-400"
                else -> "text-gray-600 dark:text-gray-400"
            }
        val valueClass =
            when (color) {
                "gray" -> "text-gray-800 dark:text-white"
                "blue" -> "text-blue-800 dark:text-blue-300"
                "green" -> "text-green-800 dark:text-green-300"
                "yellow" -> "text-yellow-800 dark:text-yellow-300"
                else -> "text-gray-800 dark:text-white"
            }

        div(classes = "$bgClass rounded-lg p-4") {
            p(classes = "text-sm $textClass") { +label }
            p(classes = "text-2xl font-bold $valueClass") { +value }
        }
    }

    private fun FlowContent.renderBackfillControls() {
        div(classes = "border-t border-gray-200 dark:border-gray-700 pt-6") {
            div(classes = "flex flex-col md:flex-row gap-4 items-start md:items-center") {
                renderThumbnailSizeSelector()
                renderBackfillButtons()
            }
            renderStatusMessages()
        }
    }

    private fun FlowContent.renderThumbnailSizeSelector() {
        div(classes = "flex-1") {
            label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                htmlFor = "thumbnailSize"
                +"Thumbnail Size"
            }
            select(
                classes = "w-full md:w-48 rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white shadow-sm focus:border-purple-500 focus:ring-purple-500",
            ) {
                attributes["id"] = "thumbnailSize"
                option {
                    value = "150"
                    +"Small (150px)"
                }
                option {
                    value = "300"
                    selected = true
                    +"Medium (300px)"
                }
                option {
                    value = "600"
                    +"Large (600px)"
                }
            }
        }
    }

    private fun FlowContent.renderBackfillButtons() {
        div(classes = "flex gap-3 mt-6 md:mt-0") {
            button(
                classes = "px-6 py-2 bg-purple-600 hover:bg-purple-700 text-white font-medium rounded-lg shadow-sm transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2",
            ) {
                attributes["id"] = "backfillThumbnailsBtn"
                unsafe { +refreshIcon() }
                span { +"Backfill Missing" }
            }
            button(
                classes = "px-6 py-2 bg-gray-600 hover:bg-gray-700 text-white font-medium rounded-lg shadow-sm transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2",
            ) {
                attributes["id"] = "regenerateThumbnailsBtn"
                unsafe { +refreshIcon() }
                span { +"Regenerate All" }
            }
        }
    }

    private fun FlowContent.renderStatusMessages() {
        div(classes = "mt-4 hidden") {
            attributes["id"] = "thumbnailStatus"
            div(classes = "rounded-lg p-4") {
                attributes["id"] = "thumbnailStatusMessage"
                div(classes = "flex items-start gap-3") {
                    unsafe { +infoIcon("w-6 h-6 flex-shrink-0", "thumbnailStatusIcon") }
                    div(classes = "flex-1") {
                        p(classes = "font-medium") { attributes["id"] = "thumbnailStatusText" }
                        p(classes = "text-sm mt-1") { attributes["id"] = "thumbnailStatusDetails" }
                    }
                }
            }
        }
    }

    private fun FlowContent.renderJobRunrDashboardLink() {
        div(classes = "mt-6 pt-6 border-t border-gray-200 dark:border-gray-700") {
            div(classes = "flex items-center justify-between p-4 bg-gray-50 dark:bg-gray-700/50 rounded-lg") {
                div {
                    p(classes = "font-medium text-gray-800 dark:text-white") {
                        +"Monitor Job Progress"
                    }
                    p(classes = "text-sm text-gray-600 dark:text-gray-400 mt-1") {
                        +"View background jobs in the JobRunr dashboard"
                    }
                }
                a(
                    href = "http://localhost:8000",
                    target = "_blank",
                    classes = "px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors",
                ) {
                    +"Open JobRunr →"
                }
            }
        }
    }

    private fun FlowContent.renderThumbnailBackfillScript(thumbnailStats: ThumbnailStats) {
        script {
            unsafe {
                raw(
                    """
                    window.thumbnailBackfillData = {
                        stats: {
                            totalArtifacts: ${thumbnailStats.totalArtifacts},
                            totalImageArtifacts: ${thumbnailStats.totalImageArtifacts},
                            withThumbnails: ${thumbnailStats.withThumbnails},
                            withoutThumbnails: ${thumbnailStats.withoutThumbnails},
                            tooLargeToProcess: ${thumbnailStats.tooLargeToProcess}
                        }
                    };
                """,
                )
            }
        }
    }
}

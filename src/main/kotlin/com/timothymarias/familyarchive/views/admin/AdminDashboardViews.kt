package com.timothymarias.familyarchive.views.admin

import com.timothymarias.familyarchive.views.AdminViewContext
import com.timothymarias.familyarchive.components.IconHelpers.addIcon
import com.timothymarias.familyarchive.components.IconHelpers.usersIcon
import com.timothymarias.familyarchive.components.adminLayout
import com.timothymarias.familyarchive.views.renderHtml
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.h3
import kotlinx.html.p
import kotlinx.html.unsafe

/**
 * View functions for admin dashboard pages.
 * Separates HTML rendering logic from controller routing/business logic.
 */
object AdminDashboardViews {
    /**
     * Renders the admin dashboard page with stats and quick actions.
     */
    fun dashboard(
        ctx: AdminViewContext,
        userCount: Long,
    ): String =
        renderHtml {
            adminLayout(
                title = "Dashboard - Admin",
                pageTitle = "Dashboard",
                isDevMode = ctx.isDevMode,
                auth = ctx.auth,
                csrf = ctx.csrf,
                successMessage = ctx.successMessage,
                errorMessage = ctx.errorMessage,
            ) {
                // Stats Cards
                div(classes = "grid grid-cols-1 md:grid-cols-1 gap-6 mb-8") {
                    div(classes = "bg-white dark:bg-gray-800 rounded-lg shadow p-6") {
                        div(classes = "flex items-center justify-between") {
                            div {
                                p(classes = "text-gray-500 dark:text-gray-400 text-sm font-medium") {
                                    +"Total Users"
                                }
                                p(classes = "text-3xl font-bold text-gray-800 dark:text-white") {
                                    +userCount.toString()
                                }
                            }
                            div(classes = "bg-blue-100 dark:bg-blue-900 rounded-full p-3") {
                                unsafe { +usersIcon("w-8 h-8 text-blue-600 dark:text-blue-400") }
                            }
                        }
                    }
                }

                // Quick Actions
                div(classes = "bg-white dark:bg-gray-800 rounded-lg shadow p-6 mb-8") {
                    h3(classes = "text-xl font-semibold text-gray-800 dark:text-white mb-4") {
                        +"Quick Actions"
                    }
                    div(classes = "grid grid-cols-1 md:grid-cols-1 gap-4") {
                        a(
                            href = "/admin/artifacts/upload",
                            classes = "flex items-center p-4 border-2 border-gray-200 dark:border-gray-700 rounded-lg hover:border-green-500 dark:hover:border-green-400 hover:bg-green-50 dark:hover:bg-green-900/20 transition-all",
                        ) {
                            div(classes = "bg-green-100 dark:bg-green-900 rounded-full p-2 mr-4") {
                                unsafe {
                                    +addIcon("w-6 h-6 text-green-600 dark:text-green-400")
                                }
                            }
                            div {
                                p(classes = "font-medium text-gray-800 dark:text-white") {
                                    +"Upload Content"
                                }
                                p(classes = "text-sm text-gray-600 dark:text-gray-400") {
                                    +"Add photos and documents"
                                }
                            }
                        }
                    }
                }

                // Recent Activity
                div(classes = "bg-white dark:bg-gray-800 rounded-lg shadow p-6") {
                    h3(classes = "text-xl font-semibold text-gray-800 dark:text-white mb-4") {
                        +"Recent Activity"
                    }
                    p(classes = "text-gray-600 dark:text-gray-400") {
                        +"No recent activity to display."
                    }
                }
            }
        }
}

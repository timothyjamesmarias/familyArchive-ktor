package com.timothymarias.familyarchive.views.admin

import com.timothymarias.familyarchive.views.AdminViewContext
import com.timothymarias.familyarchive.components.adminLayout
import com.timothymarias.familyarchive.views.renderHtml
import com.timothymarias.familyarchive.views.User
import kotlinx.html.ButtonType
import kotlinx.html.FormMethod
import kotlinx.html.InputType
import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.hiddenInput
import kotlinx.html.input
import kotlinx.html.label
import kotlinx.html.nav
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.strong
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import com.timothymarias.familyarchive.repository.Page
import java.time.format.DateTimeFormatter

/**
 * View functions for admin users pages.
 * Separates HTML rendering logic from controller routing/business logic.
 */
object AdminUsersViews {
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")

    /**
     * Renders the users index page with pagination.
     */
    fun index(
        ctx: AdminViewContext,
        users: Page<User>,
    ): String =
        renderHtml {
            adminLayout(
                title = "Users - Admin",
                pageTitle = "Users",
                isDevMode = ctx.isDevMode,
                auth = ctx.auth,
                csrf = ctx.csrf,
                successMessage = ctx.successMessage,
                errorMessage = ctx.errorMessage,
            ) {
                // Header with Add Button
                div(classes = "mb-6 flex justify-between items-center") {
                    h1(classes = "text-3xl font-bold text-gray-900 dark:text-white") { +"Users" }
                    a(href = "/admin/users/new", classes = "btn-primary") {
                        +"Add New User"
                    }
                }

                // Users Table
                div(classes = "bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden") {
                    div(classes = "table-responsive") {
                        table(classes = "min-w-full divide-y divide-gray-200 dark:divide-gray-700") {
                            thead(classes = "bg-gray-50 dark:bg-gray-700") {
                                tr {
                                    th(
                                        classes = "px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider",
                                    ) {
                                        +"Name"
                                    }
                                    th(
                                        classes = "px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider",
                                    ) {
                                        +"Email"
                                    }
                                    th(
                                        classes = "px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider",
                                    ) {
                                        +"Created At"
                                    }
                                    th(
                                        classes = "px-6 py-3 text-right text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider",
                                    ) {
                                        +"Actions"
                                    }
                                }
                            }
                            tbody(classes = "bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700") {
                                users.content.forEach { user ->
                                    tr {
                                        td(
                                            classes = "px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900 dark:text-gray-100",
                                        ) {
                                            +user.name
                                        }
                                        td(
                                            classes = "px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400",
                                        ) {
                                            +user.email
                                        }
                                        td(
                                            classes = "px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400",
                                        ) {
                                            +user.createdAt.format(DATE_FORMATTER)
                                        }
                                        td(classes = "px-6 py-4 whitespace-nowrap text-right text-sm font-medium") {
                                            a(
                                                href = "/admin/users/${user.id}/edit",
                                                classes = "text-blue-600 dark:text-blue-400 hover:text-blue-900 dark:hover:text-blue-300 mr-3",
                                            ) {
                                                +"Edit"
                                            }
                                            form(
                                                action = "/admin/users/${user.id}/delete",
                                                method = FormMethod.post,
                                                classes = "inline",
                                            ) {
                                                attributes["onsubmit"] =
                                                    "return confirm('Are you sure you want to delete this user? This action cannot be undone.');"
                                                hiddenInput(name = ctx.csrf.parameterName) {
                                                    value =
                                                        ctx.csrf.token
                                                }
                                                button(
                                                    type = ButtonType.submit,
                                                    classes = "text-red-600 dark:text-red-400 hover:text-red-900 dark:hover:text-red-300",
                                                ) {
                                                    +"Delete"
                                                }
                                            }
                                        }
                                    }
                                }
                                if (users.isEmpty) {
                                    tr {
                                        td(classes = "px-6 py-4 text-center text-sm text-gray-500 dark:text-gray-400") {
                                            attributes["colspan"] = "4"
                                            +"No users found."
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Pagination
                if (users.totalPages > 1) {
                    div(classes = "mt-6 flex items-center justify-between") {
                        div(classes = "text-sm text-gray-600 dark:text-gray-400") {
                            +"Showing "
                            span { +"${users.numberOfElements}" }
                            +" of "
                            span { +"${users.totalElements}" }
                            +" total users"
                        }
                        nav(classes = "flex items-center gap-2") {
                            // Previous Button
                            if (users.number > 0) {
                                a(
                                    href = "/admin/users?page=${users.number - 1}&size=20",
                                    classes = "px-3 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700",
                                ) {
                                    +"Previous"
                                }
                            } else {
                                span(
                                    classes = "px-3 py-2 text-sm font-medium text-gray-400 dark:text-gray-600 bg-gray-100 dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-lg cursor-not-allowed",
                                ) {
                                    +"Previous"
                                }
                            }

                            // Page Numbers
                            div(classes = "flex gap-1") {
                                for (i in 0 until users.totalPages) {
                                    if (i >= users.number - 2 && i <= users.number + 2) {
                                        if (i != users.number) {
                                            a(
                                                href = "/admin/users?page=$i&size=20",
                                                classes = "px-3 py-2 text-sm font-medium border rounded-lg bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-700",
                                            ) {
                                                +"${i + 1}"
                                            }
                                        } else {
                                            span(
                                                classes = "px-3 py-2 text-sm font-medium border rounded-lg bg-blue-600 text-white border-blue-600",
                                            ) {
                                                +"${i + 1}"
                                            }
                                        }
                                    }
                                }
                            }

                            // Next Button
                            if (users.number < users.totalPages - 1) {
                                a(
                                    href = "/admin/users?page=${users.number + 1}&size=20",
                                    classes = "px-3 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700",
                                ) {
                                    +"Next"
                                }
                            } else {
                                span(
                                    classes = "px-3 py-2 text-sm font-medium text-gray-400 dark:text-gray-600 bg-gray-100 dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-lg cursor-not-allowed",
                                ) {
                                    +"Next"
                                }
                            }
                        }
                    }
                }
            }
        }

    /**
     * Renders the new user form page.
     */
    fun new(ctx: AdminViewContext): String =
        renderHtml {
            adminLayout(
                title = "New User - Admin",
                pageTitle = "New User",
                isDevMode = ctx.isDevMode,
                auth = ctx.auth,
                csrf = ctx.csrf,
                successMessage = ctx.successMessage,
                errorMessage = ctx.errorMessage,
            ) {
                // Back link
                div(classes = "mb-6") {
                    a(href = "/admin/users", classes = "text-blue-600 dark:text-blue-400 hover:underline") {
                        +"← Back to Users"
                    }
                }

                // Page title
                div(classes = "mb-6") {
                    h1(classes = "text-3xl font-bold text-gray-900 dark:text-white") {
                        +"Create New User"
                    }
                }

                // Create Form
                div(classes = "bg-white dark:bg-gray-800 rounded-lg shadow p-6") {
                    form(action = "/admin/users", method = FormMethod.post) {
                        hiddenInput(name = ctx.csrf.parameterName) {
                            value = ctx.csrf.token
                        }

                        // Name
                        div(classes = "mb-4") {
                            label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                                htmlFor = "name"
                                +"Name "
                                span(classes = "text-red-500") { +"*" }
                            }
                            input(
                                type = InputType.text,
                                classes = "w-full rounded-lg px-3 py-2 border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white shadow-sm focus:border-blue-500 focus:ring-blue-500",
                            ) {
                                attributes["id"] = "name"
                                name = "name"
                                required = true
                            }
                        }

                        // Email
                        div(classes = "mb-4") {
                            label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                                htmlFor = "email"
                                +"Email "
                                span(classes = "text-red-500") { +"*" }
                            }
                            input(
                                type = InputType.email,
                                classes = "w-full rounded-lg px-3 py-2 border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white shadow-sm focus:border-blue-500 focus:ring-blue-500",
                            ) {
                                attributes["id"] = "email"
                                name = "email"
                                required = true
                            }
                        }

                        // Password
                        div(classes = "mb-4") {
                            label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                                htmlFor = "password"
                                +"Password "
                                span(classes = "text-red-500") { +"*" }
                            }
                            input(
                                type = InputType.password,
                                classes = "w-full rounded-lg px-3 py-2 border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white shadow-sm focus:border-blue-500 focus:ring-blue-500",
                            ) {
                                attributes["id"] = "password"
                                name = "password"
                                required = true
                                attributes["minlength"] = "8"
                            }
                            p(classes = "mt-1 text-xs text-gray-500 dark:text-gray-400") {
                                +"Password must be at least 8 characters long"
                            }
                        }

                        // Buttons
                        div(classes = "flex gap-2 mt-6") {
                            button(type = ButtonType.submit, classes = "btn-primary") {
                                +"Create User"
                            }
                            a(href = "/admin/users", classes = "btn-secondary") {
                                +"Cancel"
                            }
                        }
                    }
                }
            }
        }

    /**
     * Renders the user edit page.
     */
    fun edit(
        ctx: AdminViewContext,
        user: User,
    ): String =
        renderHtml {
            adminLayout(
                title = "Edit User - Admin",
                pageTitle = "Edit User",
                isDevMode = ctx.isDevMode,
                auth = ctx.auth,
                csrf = ctx.csrf,
                successMessage = ctx.successMessage,
                errorMessage = ctx.errorMessage,
            ) {
                // Back link
                div(classes = "mb-6") {
                    a(href = "/admin/users", classes = "text-blue-600 dark:text-blue-400 hover:underline") {
                        +"← Back to Users"
                    }
                }

                // Page title
                div(classes = "mb-6") {
                    h1(classes = "text-3xl font-bold text-gray-900 dark:text-white") {
                        +"Edit User"
                    }
                }

                // Edit Form
                div(classes = "bg-white dark:bg-gray-800 rounded-lg shadow p-6") {
                    form(action = "/admin/users/${user.id}", method = FormMethod.post) {
                        hiddenInput(name = ctx.csrf.parameterName) {
                            value = ctx.csrf.token
                        }

                        // Name
                        div(classes = "mb-4") {
                            label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                                htmlFor = "name"
                                +"Name "
                                span(classes = "text-red-500") { +"*" }
                            }
                            input(
                                type = InputType.text,
                                classes = "w-full rounded-lg px-3 py-2 border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white shadow-sm focus:border-blue-500 focus:ring-blue-500",
                            ) {
                                attributes["id"] = "name"
                                name = "name"
                                value = user.name
                                required = true
                            }
                        }

                        // Email
                        div(classes = "mb-4") {
                            label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                                htmlFor = "email"
                                +"Email "
                                span(classes = "text-red-500") { +"*" }
                            }
                            input(
                                type = InputType.email,
                                classes = "w-full rounded-lg px-3 py-2 border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white shadow-sm focus:border-blue-500 focus:ring-blue-500",
                            ) {
                                attributes["id"] = "email"
                                name = "email"
                                value = user.email
                                required = true
                            }
                        }

                        // Password (optional)
                        div(classes = "mb-4") {
                            label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                                htmlFor = "password"
                                +"New Password (optional)"
                            }
                            input(
                                type = InputType.password,
                                classes = "w-full rounded-lg px-3 py-2 border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white shadow-sm focus:border-blue-500 focus:ring-blue-500",
                            ) {
                                attributes["id"] = "password"
                                name = "password"
                                attributes["minlength"] = "8"
                            }
                            p(classes = "mt-1 text-xs text-gray-500 dark:text-gray-400") {
                                +"Leave blank to keep the current password. If provided, must be at least 8 characters long."
                            }
                        }

                        // Buttons
                        div(classes = "flex gap-2 mt-6") {
                            button(type = ButtonType.submit, classes = "btn-primary") {
                                +"Save Changes"
                            }
                            a(href = "/admin/users", classes = "btn-secondary") {
                                +"Cancel"
                            }
                        }
                    }
                }

                // User Info
                div(classes = "mt-6 bg-gray-50 dark:bg-gray-900 rounded-lg p-4") {
                    p(classes = "text-sm text-gray-600 dark:text-gray-400") {
                        strong { +"Created at: " }
                        +user.createdAt.format(DATE_FORMATTER)
                    }
                    p(classes = "text-sm text-gray-600 dark:text-gray-400 mt-2") {
                        strong { +"Last updated: " }
                        +user.updatedAt.format(DATE_FORMATTER)
                    }
                }
            }
        }
}

package com.timothymarias.familyarchive.views.admin

import com.timothymarias.familyarchive.views.AdminViewContext
import com.timothymarias.familyarchive.components.adminLayout
import com.timothymarias.familyarchive.views.renderHtml
import com.timothymarias.familyarchive.views.Place
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
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import com.timothymarias.familyarchive.repository.Page

/**
 * View functions for admin places pages.
 * Separates HTML rendering logic from controller routing/business logic.
 */
object AdminPlacesViews {
    /**
     * Renders the places index page with search and pagination.
     */
    fun index(
        ctx: AdminViewContext,
        places: Page<Place>,
        search: String?,
    ): String =
        renderHtml {
            adminLayout(
                title = "Places - Admin",
                pageTitle = "Places",
                isDevMode = ctx.isDevMode,
                auth = ctx.auth,
                csrf = ctx.csrf,
                successMessage = ctx.successMessage,
                errorMessage = ctx.errorMessage,
            ) {
                div(classes = "mb-6 flex justify-between items-center") {
                    h1(classes = "text-3xl font-bold text-gray-900 dark:text-white") { +"Places" }
                }
                div(classes = "mb-6") {
                    form(action = "/admin/places", method = FormMethod.get, classes = "flex gap-2") {
                        input(
                            type = InputType.text,
                            name = "search",
                            classes = "flex-1 rounded-lg px-2 border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white shadow-sm focus:border-blue-500 focus:ring-blue-500",
                        ) {
                            value = search ?: ""
                            placeholder = "Search places..."
                        }
                        button(type = ButtonType.submit, classes = "btn-primary") { +"Search" }
                        if (!search.isNullOrBlank()) {
                            a(href = "/admin/places", classes = "btn-secondary") { +"Clear" }
                        }
                    }
                }
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
                                        +"Created"
                                    }
                                    th(
                                        classes = "px-6 py-3 text-right text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider",
                                    ) {
                                        +"Actions"
                                    }
                                }
                            }
                            tbody(classes = "bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700") {
                                if (places.isEmpty) {
                                    tr {
                                        td(classes = "px-6 py-4 text-center text-sm text-gray-500 dark:text-gray-400") {
                                            attributes["colspan"] = "3"
                                            if (!search.isNullOrBlank()) {
                                                +"No places found matching \"$search\""
                                            } else {
                                                +"No places found. Import a GEDCOM file to get started."
                                            }
                                        }
                                    }
                                } else {
                                    places.content.forEach { place ->
                                        tr {
                                            td(
                                                classes = "px-6 py-4 text-sm font-medium text-gray-900 dark:text-gray-100",
                                            ) {
                                                span { +place.name }
                                            }
                                            td(
                                                classes = "px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400",
                                            ) {
                                                span { +place.createdAt.toString() }
                                            }
                                            td(classes = "px-6 py-4 whitespace-nowrap text-right text-sm font-medium") {
                                                a(
                                                    href = "/admin/places/${place.id}",
                                                    classes = "text-blue-600 dark:text-blue-400 hover:text-blue-900 dark:hover:text-blue-300 mr-3",
                                                ) {
                                                    +"View"
                                                }
                                                form(
                                                    action = "/admin/places/${place.id}/delete",
                                                    method = FormMethod.post,
                                                    classes = "inline",
                                                ) {
                                                    attributes["onsubmit"] =
                                                        "return confirm('Are you sure you want to delete this place? This action cannot be undone.');"
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
                                }
                            }
                        }
                    }
                }
            }
        }

    /**
     * Renders the new place form page.
     */
    fun new(ctx: AdminViewContext): String =
        renderHtml {
            adminLayout(
                title = "New Place - Admin",
                pageTitle = "New Place",
                isDevMode = ctx.isDevMode,
                auth = ctx.auth,
                csrf = ctx.csrf,
                successMessage = ctx.successMessage,
                errorMessage = ctx.errorMessage,
            ) {
                div(classes = "bg-white dark:bg-gray-800 rounded-lg shadow p-6") {
                    p(classes = "text-gray-600 dark:text-gray-400") {
                        +"Create new place form"
                    }
                }
            }
        }

    /**
     * Renders the place show/details page.
     */
    fun show(
        ctx: AdminViewContext,
        place: Place,
    ): String =
        renderHtml {
            adminLayout(
                title = "Place Details - Admin",
                pageTitle = "Place Details",
                isDevMode = ctx.isDevMode,
                auth = ctx.auth,
                csrf = ctx.csrf,
                successMessage = ctx.successMessage,
                errorMessage = ctx.errorMessage,
            ) {
                div(classes = "bg-white dark:bg-gray-800 rounded-lg shadow p-6") {
                    p(classes = "text-gray-600 dark:text-gray-400") {
                        +"Place: ${place.name}"
                    }
                }
            }
        }

    /**
     * Renders the place edit page.
     */
    fun edit(
        ctx: AdminViewContext,
        place: Place,
    ): String =
        renderHtml {
            adminLayout(
                title = "Edit Place - Admin",
                pageTitle = "Edit Place",
                isDevMode = ctx.isDevMode,
                auth = ctx.auth,
                csrf = ctx.csrf,
                successMessage = ctx.successMessage,
                errorMessage = ctx.errorMessage,
            ) {
                div(classes = "bg-white dark:bg-gray-800 rounded-lg shadow p-6") {
                    p(classes = "text-gray-600 dark:text-gray-400") {
                        +"Edit place: ${place.name}"
                    }
                }
            }
        }
}

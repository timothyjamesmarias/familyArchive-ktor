package com.timothymarias.familyarchive.components

import com.timothymarias.familyarchive.views.Artifact
import kotlinx.html.ButtonType
import kotlinx.html.FormMethod
import kotlinx.html.MAIN
import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.code
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.hiddenInput
import kotlinx.html.span
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import com.timothymarias.familyarchive.views.CsrfInfo

fun MAIN.artifactTypeTable(
    artifacts: List<Artifact>,
    typeName: String,
    routeSegment: String,
    csrf: CsrfInfo,
) {
    div(classes = "mb-6 flex justify-between items-center") {
        h1(classes = "text-3xl font-bold text-gray-900 dark:text-white") { +typeName }
        a(href = "/admin/artifacts/upload", classes = "btn-primary") {
            +"Upload Artifact"
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
                            +"Title"
                        }
                        th(
                            classes = "px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider",
                        ) {
                            +"Slug"
                        }
                        th(
                            classes = "px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider",
                        ) {
                            +"Uploaded"
                        }
                        th(
                            classes = "px-6 py-3 text-right text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider",
                        ) {
                            +"Actions"
                        }
                    }
                }
                tbody(classes = "bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700") {
                    artifacts.forEach { artifact ->
                        tr {
                            td(classes = "px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-100") {
                                span { +(artifact.title ?: "(Untitled)") }
                            }
                            td(classes = "px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400") {
                                code { +artifact.slug }
                            }
                            td(classes = "px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400") {
                                span {
                                    +artifact.uploadedAt.format(
                                        java.time.format.DateTimeFormatter
                                            .ofPattern("MMM dd, yyyy"),
                                    )
                                }
                            }
                            td(classes = "px-6 py-4 whitespace-nowrap text-right text-sm font-medium") {
                                a(
                                    href = "/admin/artifacts/$routeSegment/${artifact.id}/edit",
                                    classes = "text-blue-600 dark:text-blue-400 hover:text-blue-900 dark:hover:text-blue-300 mr-3",
                                ) {
                                    +"Edit"
                                }
                                form(
                                    action = "/admin/artifacts/$routeSegment/${artifact.id}/delete",
                                    method = FormMethod.post,
                                    classes = "inline",
                                ) {
                                    attributes["onsubmit"] =
                                        "return confirm('Are you sure you want to delete this ${typeName.lowercase().removeSuffix(
                                            "s",
                                        )}?');"
                                    hiddenInput(name = csrf.parameterName) { value = csrf.token }
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
                    if (artifacts.isEmpty()) {
                        tr {
                            td(classes = "px-6 py-4 text-center text-sm text-gray-500 dark:text-gray-400") {
                                attributes["colspan"] = "4"
                                +"No ${typeName.lowercase()} found. "
                                a(
                                    href = "/admin/artifacts/upload",
                                    classes = "text-blue-600 dark:text-blue-400 hover:underline",
                                ) {
                                    +"Upload your first ${typeName.lowercase().removeSuffix("s")}"
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

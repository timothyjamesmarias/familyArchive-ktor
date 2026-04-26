package com.timothymarias.familyarchive.views.admin

import com.timothymarias.familyarchive.views.AdminViewContext
import com.timothymarias.familyarchive.components.adminLayout
import com.timothymarias.familyarchive.components.artifactEditForm
import com.timothymarias.familyarchive.views.renderHtml
import com.timothymarias.familyarchive.views.Artifact
import com.timothymarias.familyarchive.model.ArtifactType
import kotlinx.html.ButtonType
import kotlinx.html.FormMethod
import kotlinx.html.InputType
import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.code
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.hiddenInput
import kotlinx.html.input
import kotlinx.html.label
import kotlinx.html.nav
import kotlinx.html.option
import kotlinx.html.p
import kotlinx.html.select
import kotlinx.html.span
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import com.timothymarias.familyarchive.repository.Page

/**
 * View functions for admin artifacts pages.
 * Separates HTML rendering logic from controller routing/business logic.
 */
object AdminArtifactsViews {
    /**
     * Renders the artifacts index page with pagination.
     */
    fun index(
        ctx: AdminViewContext,
        artifacts: Page<Artifact>,
    ): String =
        renderHtml {
            adminLayout(
                title = "Artifacts - Admin",
                pageTitle = "Artifacts",
                isDevMode = ctx.isDevMode,
                auth = ctx.auth,
                csrf = ctx.csrf,
                successMessage = ctx.successMessage,
                errorMessage = ctx.errorMessage,
            ) {
                div(classes = "mb-6 flex justify-between items-center") {
                    h1(classes = "text-3xl font-bold text-gray-900 dark:text-white") { +"Artifacts" }
                    div {
                        a(href = "/admin/artifacts/upload", classes = "btn-primary") {
                            +"Upload Artifact"
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
                                        +"Type"
                                    }
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
                                artifacts.content.forEach { artifact ->
                                    tr {
                                        td(classes = "px-6 py-4 whitespace-nowrap") {
                                            span(
                                                classes = "px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-200",
                                            ) {
                                                +artifact.artifactType.displayName
                                            }
                                        }
                                        td(
                                            classes = "px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-100",
                                        ) {
                                            span { +(artifact.title ?: "(Untitled)") }
                                        }
                                        td(
                                            classes = "px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400",
                                        ) {
                                            code { +artifact.slug }
                                        }
                                        td(
                                            classes = "px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400",
                                        ) {
                                            span {
                                                +artifact.uploadedAt.format(
                                                    java.time.format.DateTimeFormatter
                                                        .ofPattern("MMM dd, yyyy"),
                                                )
                                            }
                                        }
                                        td(classes = "px-6 py-4 whitespace-nowrap text-right text-sm font-medium") {
                                            a(
                                                href = "/admin/artifacts/${artifact.id}/edit",
                                                classes = "text-blue-600 dark:text-blue-400 hover:text-blue-900 dark:hover:text-blue-300 mr-3",
                                            ) {
                                                +"Edit"
                                            }
                                            form(
                                                action = "/admin/artifacts/${artifact.id}/delete",
                                                method = FormMethod.post,
                                                classes = "inline",
                                            ) {
                                                attributes["onsubmit"] =
                                                    "return confirm('Are you sure you want to delete this artifact?');"
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
                                if (artifacts.isEmpty) {
                                    tr {
                                        td(classes = "px-6 py-4 text-center text-sm text-gray-500 dark:text-gray-400") {
                                            attributes["colspan"] = "5"
                                            +"No artifacts found. "
                                            a(
                                                href = "/admin/artifacts/upload",
                                                classes = "text-blue-600 dark:text-blue-400 hover:underline",
                                            ) {
                                                +"Upload your first artifact"
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Pagination
                    if (artifacts.totalPages > 1) {
                        div(
                            classes = "bg-gray-50 dark:bg-gray-700 px-4 py-3 flex items-center justify-between border-t border-gray-200 dark:border-gray-600 sm:px-6",
                        ) {
                            div(classes = "flex-1 flex justify-between sm:hidden") {
                                if (!artifacts.isFirst) {
                                    a(
                                        href = "/admin/artifacts?page=${artifacts.number - 1}",
                                        classes = "relative inline-flex items-center px-4 py-2 border border-gray-300 dark:border-gray-600 text-sm font-medium rounded-md text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700",
                                    ) {
                                        +"Previous"
                                    }
                                }
                                if (!artifacts.isLast) {
                                    a(
                                        href = "/admin/artifacts?page=${artifacts.number + 1}",
                                        classes = "ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 dark:border-gray-600 text-sm font-medium rounded-md text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700",
                                    ) {
                                        +"Next"
                                    }
                                }
                            }
                            div(classes = "hidden sm:flex-1 sm:flex sm:items-center sm:justify-between") {
                                div {
                                    p(classes = "text-sm text-gray-700 dark:text-gray-300") {
                                        +"Showing page "
                                        span { +"${artifacts.number + 1}" }
                                        +" of "
                                        span { +"${artifacts.totalPages}" }
                                    }
                                }
                                div {
                                    nav(classes = "relative z-0 inline-flex rounded-md shadow-sm -space-x-px") {
                                        if (!artifacts.isFirst) {
                                            a(
                                                href = "/admin/artifacts?page=${artifacts.number - 1}",
                                                classes = "relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-800 text-sm font-medium text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-700",
                                            ) {
                                                +"Previous"
                                            }
                                        }
                                        if (!artifacts.isLast) {
                                            a(
                                                href = "/admin/artifacts?page=${artifacts.number + 1}",
                                                classes = "relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-800 text-sm font-medium text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-700",
                                            ) {
                                                +"Next"
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
     * Renders the upload artifact form.
     */
    fun uploadForm(
        ctx: AdminViewContext,
        artifactTypes: List<ArtifactType>,
    ): String =
        renderHtml {
            adminLayout(
                title = "Upload Artifact - Admin",
                pageTitle = "Upload Artifact",
                isDevMode = ctx.isDevMode,
                auth = ctx.auth,
                csrf = ctx.csrf,
                successMessage = ctx.successMessage,
                errorMessage = ctx.errorMessage,
            ) {
                div(classes = "mb-6") {
                    h1(classes = "text-3xl font-bold text-gray-900 dark:text-white") { +"Upload Artifact" }
                    p(classes = "mt-2 text-sm text-gray-600 dark:text-gray-400") {
                        +"Upload a new artifact to the archive. You'll be able to add more details after upload."
                    }
                }

                div(classes = "bg-white dark:bg-gray-800 rounded-lg shadow p-6") {
                    form(action = "/admin/artifacts/upload", method = FormMethod.post, classes = "space-y-6") {
                        attributes["id"] = "uploadForm"
                        attributes["enctype"] = "multipart/form-data"
                        hiddenInput(name = ctx.csrf.parameterName) { value = ctx.csrf.token }

                        // Uppy File Upload
                        div {
                            label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                                +"Files *"
                            }
                            div {
                                attributes["data-uppy-uploader"] = "true"
                                attributes["data-form-id"] = "uploadForm"
                                attributes["data-field-name"] = "files"
                                attributes["data-allowed-types"] = "image/*,application/pdf,audio/*,video/*"
                                attributes["data-max-file-size"] = "52428800"
                                attributes["data-max-number-of-files"] = "10"
                            }
                            p(classes = "mt-2 text-sm text-gray-500 dark:text-gray-400") {
                                +"Supported formats: Images, PDFs, Audio, and Video files. Maximum 50MB per file, up to 10 files."
                            }
                        }

                        // Artifact Type
                        div {
                            label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                                htmlFor = "artifactType"
                                +"Artifact Type *"
                            }
                            select(
                                classes = "block w-full px-4 py-2 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400",
                            ) {
                                attributes["id"] = "artifactType"
                                name = "artifactType"
                                required = true
                                option {
                                    value = ""
                                    +"Select type..."
                                }
                                artifactTypes.forEach { type ->
                                    option {
                                        value = type.name
                                        +type.displayName
                                    }
                                }
                            }
                        }

                        // Title (optional)
                        div {
                            label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                                htmlFor = "title"
                                +"Title (optional)"
                            }
                            input(
                                type = InputType.text,
                                name = "title",
                                classes = "block w-full px-4 py-2 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400",
                            ) {
                                attributes["id"] = "title"
                                placeholder = "e.g., Family Portrait 1945"
                            }
                        }

                        // Original Date String (optional)
                        div {
                            label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                                htmlFor = "originalDateString"
                                +"Original Date (optional)"
                            }
                            input(
                                type = InputType.text,
                                name = "originalDateString",
                                classes = "block w-full px-4 py-2 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400",
                            ) {
                                attributes["id"] = "originalDateString"
                                placeholder = "e.g., circa 1945, Summer 1950s, Unknown"
                            }
                            p(classes = "mt-1 text-sm text-gray-500 dark:text-gray-400") {
                                +"Free-form text describing when this artifact was created"
                            }
                        }

                        // Actions
                        div(classes = "flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 pt-4") {
                            a(
                                href = "/admin/artifacts",
                                classes = "text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white text-center sm:text-left",
                            ) {
                                +"Cancel"
                            }
                            div(classes = "flex flex-col sm:flex-row gap-2 w-full sm:w-auto") {
                                button(
                                    type = ButtonType.submit,
                                    name = "action",
                                    classes = "btn-primary w-full sm:w-auto",
                                ) {
                                    value = "save"
                                    +"Upload Artifact"
                                }
                                button(
                                    type = ButtonType.submit,
                                    name = "action",
                                    classes = "w-full sm:w-auto px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 dark:bg-gray-500 dark:hover:bg-gray-600",
                                ) {
                                    value = "save_and_add_another"
                                    +"Upload and Add Another"
                                }
                            }
                        }
                    }
                }
            }
        }

    /**
     * Renders the edit artifact form.
     */
    fun edit(
        ctx: AdminViewContext,
        artifact: Artifact,
        artifactTypes: List<ArtifactType>,
    ): String =
        renderHtml {
            adminLayout(
                title = "Edit Artifact - Admin",
                pageTitle = "Edit Artifact",
                isDevMode = ctx.isDevMode,
                auth = ctx.auth,
                csrf = ctx.csrf,
                successMessage = ctx.successMessage,
                errorMessage = ctx.errorMessage,
            ) {
                artifactEditForm(artifact, "Artifact", "artifacts", artifactTypes, ctx.csrf)
            }
        }
}

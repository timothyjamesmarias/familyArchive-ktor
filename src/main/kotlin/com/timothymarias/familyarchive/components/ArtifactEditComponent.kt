package com.timothymarias.familyarchive.components

import com.timothymarias.familyarchive.views.Artifact
import com.timothymarias.familyarchive.model.ArtifactType
import kotlinx.html.ButtonType
import kotlinx.html.FormMethod
import kotlinx.html.InputType
import kotlinx.html.MAIN
import kotlinx.html.a
import kotlinx.html.audio
import kotlinx.html.button
import kotlinx.html.code
import kotlinx.html.dd
import kotlinx.html.div
import kotlinx.html.dl
import kotlinx.html.dt
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.h3
import kotlinx.html.hiddenInput
import kotlinx.html.img
import kotlinx.html.input
import kotlinx.html.label
import kotlinx.html.option
import kotlinx.html.p
import kotlinx.html.select
import kotlinx.html.source
import kotlinx.html.span
import kotlinx.html.video
import com.timothymarias.familyarchive.views.CsrfInfo

fun MAIN.artifactEditForm(
    artifact: Artifact,
    typeName: String,
    routeSegment: String,
    artifactTypes: List<ArtifactType>,
    csrf: CsrfInfo,
) {
    // Header with Back link
    div(classes = "mb-6") {
        div(classes = "flex justify-between items-center") {
            div {
                h1(classes = "text-3xl font-bold text-gray-900 dark:text-white") {
                    +"Edit $typeName"
                }
                p(classes = "mt-2 text-sm text-gray-600 dark:text-gray-400") {
                    +"Slug: "
                    code(classes = "bg-gray-100 dark:bg-gray-700 px-2 py-1 rounded") {
                        +artifact.slug
                    }
                }
            }
            a(
                href = "/admin/artifacts/$routeSegment",
                classes = "text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white",
            ) {
                +"Back to ${typeName}s"
            }
        }
    }

    // Main Content
    div(classes = "grid grid-cols-1 lg:grid-cols-3 gap-6") {
        // Basic Info Form (left column)
        div(classes = "lg:col-span-2 space-y-6") {
            div(classes = "bg-white dark:bg-gray-800 rounded-lg shadow p-6") {
                h2(classes = "text-xl font-semibold text-gray-900 dark:text-white mb-4") {
                    +"Basic Information"
                }
                form(
                    action = "/admin/artifacts/$routeSegment/${artifact.id}",
                    method = FormMethod.post,
                    classes = "space-y-4",
                ) {
                    hiddenInput(name = csrf.parameterName) {
                        value = csrf.token
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
                            artifactTypes.forEach { type ->
                                option {
                                    value = type.name
                                    selected = (type == artifact.artifactType)
                                    +type.displayName
                                }
                            }
                        }
                        p(classes = "mt-1 text-xs text-gray-500 dark:text-gray-400") {
                            +"Changing the type will redirect you to the appropriate edit page"
                        }
                    }

                    // Title
                    div {
                        label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                            htmlFor = "title"
                            +"Title"
                        }
                        input(
                            type = InputType.text,
                            name = "title",
                            classes = "block w-full px-4 py-2 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400",
                        ) {
                            attributes["id"] = "title"
                            value = artifact.title ?: ""
                            placeholder = "e.g., Family Portrait 1945"
                        }
                    }

                    // Original Date String
                    div {
                        label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                            htmlFor = "originalDateString"
                            +"Original Date"
                        }
                        input(
                            type = InputType.text,
                            name = "originalDateString",
                            classes = "block w-full px-4 py-2 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400",
                        ) {
                            attributes["id"] = "originalDateString"
                            value = artifact.originalDateString ?: ""
                            placeholder = "e.g., circa 1945, Summer 1950s"
                        }
                    }

                    // Actions
                    div(classes = "flex items-center justify-between pt-4") {
                        button(type = ButtonType.submit, classes = "btn-primary") {
                            +"Save Changes"
                        }
                    }
                }
            }
        }

        // Sidebar
        div(classes = "space-y-6") {
            // File Info with previews
            div(classes = "bg-white dark:bg-gray-800 rounded-lg shadow p-6") {
                h2(classes = "text-xl font-semibold text-gray-900 dark:text-white mb-4") {
                    +"Files ("
                    span { +"${artifact.files.size}" }
                    +")"
                }

                // If no files, show legacy single file info with preview
                if (artifact.files.isEmpty()) {
                    div(classes = "space-y-4") {
                        // Show preview based on MIME type
                        artifact.mimeType?.let { mimeType ->
                            when {
                                mimeType.startsWith("image/") -> {
                                    div(
                                        classes = "mb-3 flex justify-center items-center bg-gray-100 dark:bg-gray-700 rounded-lg p-3",
                                    ) {
                                        img(
                                            src = "/uploads/${artifact.storagePath}",
                                            alt = "Artifact",
                                            classes = "max-w-full max-h-64 object-contain rounded",
                                        )
                                    }
                                }
                                mimeType.startsWith("video/") -> {
                                    div(
                                        classes = "mb-3 flex justify-center items-center bg-gray-100 dark:bg-gray-700 rounded-lg p-3",
                                    ) {
                                        video(classes = "max-w-full max-h-64 rounded") {
                                            attributes["controls"] = "controls"
                                            source {
                                                src = "/uploads/${artifact.storagePath}"
                                                type = mimeType
                                            }
                                            +"Your browser does not support the video tag."
                                        }
                                    }
                                }
                                mimeType.startsWith("audio/") -> {
                                    div(classes = "mb-3") {
                                        audio(classes = "w-full") {
                                            attributes["controls"] = "controls"
                                            source {
                                                src = "/uploads/${artifact.storagePath}"
                                                type = mimeType
                                            }
                                            +"Your browser does not support the audio tag."
                                        }
                                    }
                                }
                            }
                        }

                        dl(classes = "space-y-3") {
                            dt(classes = "text-sm font-medium text-gray-500 dark:text-gray-400") { +"MIME Type" }
                            dd(classes = "mt-1 text-sm text-gray-900 dark:text-gray-100 mb-3") {
                                code { +(artifact.mimeType ?: "Unknown") }
                            }
                            dt(classes = "text-sm font-medium text-gray-500 dark:text-gray-400") { +"File Size" }
                            dd(classes = "mt-1 text-sm text-gray-900 dark:text-gray-100 mb-3") {
                                +"${String.format("%.2f", artifact.fileSize / 1024.0)} KB"
                            }
                            dt(classes = "text-sm font-medium text-gray-500 dark:text-gray-400") { +"Uploaded" }
                            dd(classes = "mt-1 text-sm text-gray-900 dark:text-gray-100 mb-3") {
                                +artifact.uploadedAt.format(
                                    java.time.format.DateTimeFormatter
                                        .ofPattern("MMM dd, yyyy HH:mm"),
                                )
                            }
                            dt(classes = "text-sm font-medium text-gray-500 dark:text-gray-400") { +"Storage Path" }
                            dd(classes = "mt-1 text-xs text-gray-600 dark:text-gray-400 break-all") {
                                code { +(artifact.storagePath ?: "") }
                            }
                        }
                    }
                }

                // Multiple Files
                if (artifact.files.isNotEmpty()) {
                    div(classes = "space-y-4") {
                        artifact.files.forEach { file ->
                            div(classes = "border border-gray-200 dark:border-gray-700 rounded-lg p-4") {
                                div(classes = "flex justify-between items-start mb-3") {
                                    h3(classes = "text-sm font-semibold text-gray-900 dark:text-white") {
                                        +"File ${file.fileSequence}"
                                    }
                                    div(classes = "flex items-center gap-2") {
                                        span(classes = "text-xs text-gray-500 dark:text-gray-400") {
                                            +"${String.format("%.2f", file.fileSize / 1024.0)} KB"
                                        }
                                        form(
                                            action = "/admin/artifacts/$routeSegment/${artifact.id}/files/${file.id}/delete",
                                            method = FormMethod.post,
                                            classes = "inline",
                                        ) {
                                            attributes["onsubmit"] =
                                                "return confirm('Are you sure you want to delete this file?');"
                                            hiddenInput(name = csrf.parameterName) {
                                                value = csrf.token
                                            }
                                            button(
                                                type = ButtonType.submit,
                                                classes = "text-red-600 dark:text-red-400 hover:text-red-800 dark:hover:text-red-300 text-xs",
                                            ) {
                                                +"Delete"
                                            }
                                        }
                                    }
                                }

                                // Image Preview
                                if (file.mimeType.startsWith("image/")) {
                                    div(
                                        classes = "mb-3 flex justify-center items-center bg-gray-100 dark:bg-gray-700 rounded-lg p-3",
                                    ) {
                                        img(
                                            src = "/uploads/${file.storagePath}",
                                            alt = "File ${file.fileSequence}",
                                            classes = "max-w-full max-h-64 object-contain rounded",
                                        )
                                    }
                                }

                                // Video Preview
                                if (file.mimeType.startsWith("video/")) {
                                    div(
                                        classes = "mb-3 flex justify-center items-center bg-gray-100 dark:bg-gray-700 rounded-lg p-3",
                                    ) {
                                        video(classes = "max-w-full max-h-64 rounded") {
                                            attributes["controls"] = "controls"
                                            source {
                                                src = "/uploads/${file.storagePath}"
                                                type = file.mimeType
                                            }
                                            +"Your browser does not support the video tag."
                                        }
                                    }
                                }

                                // Audio Preview
                                if (file.mimeType.startsWith("audio/")) {
                                    div(classes = "mb-3") {
                                        audio(classes = "w-full") {
                                            attributes["controls"] = "controls"
                                            source {
                                                src = "/uploads/${file.storagePath}"
                                                type = file.mimeType
                                            }
                                            +"Your browser does not support the audio tag."
                                        }
                                    }
                                }

                                // File Details
                                dl(classes = "space-y-2 text-xs") {
                                    dt(classes = "text-gray-500 dark:text-gray-400") { +"MIME Type" }
                                    dd(classes = "text-gray-900 dark:text-gray-100 mb-2") {
                                        code { +file.mimeType }
                                    }
                                    dt(classes = "text-gray-500 dark:text-gray-400") { +"Uploaded" }
                                    dd(classes = "text-gray-900 dark:text-gray-100 mb-2") {
                                        +file.uploadedAt.format(
                                            java.time.format.DateTimeFormatter
                                                .ofPattern("MMM dd, yyyy HH:mm"),
                                        )
                                    }
                                    dt(classes = "text-gray-500 dark:text-gray-400") { +"Storage Path" }
                                    dd(classes = "text-gray-600 dark:text-gray-400 break-all") {
                                        code { +file.storagePath }
                                    }
                                }
                            }
                        }
                    }
                }

                // Artifact Info
                div(classes = "mt-4 pt-4 border-t border-gray-200 dark:border-gray-700") {
                    h3(classes = "text-sm font-semibold text-gray-900 dark:text-white mb-3") { +"Artifact Info" }
                    dl(classes = "space-y-2 text-xs") {
                        dt(classes = "text-gray-500 dark:text-gray-400") { +"Uploaded" }
                        dd(classes = "text-gray-900 dark:text-gray-100 mb-2") {
                            +artifact.uploadedAt.format(
                                java.time.format.DateTimeFormatter
                                    .ofPattern("MMM dd, yyyy HH:mm"),
                            )
                        }
                        dt(classes = "text-gray-500 dark:text-gray-400") { +"Last Updated" }
                        dd(classes = "text-gray-900 dark:text-gray-100") {
                            +artifact.updatedAt.format(
                                java.time.format.DateTimeFormatter
                                    .ofPattern("MMM dd, yyyy HH:mm"),
                            )
                        }
                    }
                }
            }

            // Annotations (only for photos)
            if (artifact.artifactType == ArtifactType.PHOTO) {
                div(classes = "bg-white dark:bg-gray-800 rounded-lg shadow p-6") {
                    h2(classes = "text-xl font-semibold text-gray-900 dark:text-white mb-4") {
                        +"Annotations"
                    }
                    p(classes = "text-sm text-gray-600 dark:text-gray-400 mb-4") {
                        +"Add markers and labels to identify people in this photo"
                    }
                    a(
                        href = "/admin/artifacts/$routeSegment/${artifact.id}/annotations",
                        classes = "btn-primary block text-center w-full",
                    ) {
                        +"Edit Annotations"
                    }
                }
            }

            // Danger Zone
            div(
                classes = "bg-white dark:bg-gray-800 rounded-lg shadow p-6 border-2 border-red-200 dark:border-red-900",
            ) {
                h3(classes = "text-lg font-semibold text-red-600 dark:text-red-400 mb-3") {
                    +"Danger Zone"
                }
                p(classes = "text-sm text-gray-600 dark:text-gray-400 mb-4") {
                    +"Deleting this ${typeName.lowercase()} is permanent and cannot be undone."
                }
                form(action = "/admin/artifacts/$routeSegment/${artifact.id}/delete", method = FormMethod.post) {
                    attributes["onsubmit"] =
                        "return confirm('Are you sure you want to delete this ${typeName.lowercase()}? This action cannot be undone.');"
                    hiddenInput(name = csrf.parameterName) {
                        value = csrf.token
                    }
                    button(
                        type = ButtonType.submit,
                        classes = "w-full px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 dark:bg-red-700 dark:hover:bg-red-800 transition-colors",
                    ) {
                        +"Delete $typeName"
                    }
                }
            }

            // Add More Files Section
            div(classes = "bg-white dark:bg-gray-800 rounded-lg shadow p-6") {
                h2(classes = "text-xl font-semibold text-gray-900 dark:text-white mb-4") {
                    +"Add More Files"
                }
                p(classes = "text-sm text-gray-600 dark:text-gray-400 mb-4") {
                    +"Upload additional files to this artifact"
                }

                form(
                    action = "/admin/artifacts/$routeSegment/${artifact.id}/files",
                    method = FormMethod.post,
                    classes = "space-y-4",
                ) {
                    attributes["id"] = "addFilesForm"
                    attributes["enctype"] = "multipart/form-data"
                    hiddenInput(name = csrf.parameterName) {
                        value = csrf.token
                    }

                    // Uppy File Upload
                    div {
                        label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                            +"Files"
                        }
                        div {
                            attributes["data-uppy-uploader"] = "true"
                            attributes["data-form-id"] = "addFilesForm"
                            attributes["data-field-name"] = "files"
                            attributes["data-allowed-types"] = "image/*,application/pdf,audio/*,video/*"
                            attributes["data-max-file-size"] = "52428800"
                            attributes["data-max-number-of-files"] = "10"
                        }
                        p(classes = "mt-2 text-sm text-gray-500 dark:text-gray-400") {
                            +"Supported formats: Images, PDFs, Audio, and Video files. Maximum 50MB per file, up to 10 files."
                        }
                    }

                    // Submit Button
                    div(classes = "pt-2") {
                        button(type = ButtonType.submit, classes = "btn-primary w-full") {
                            +"Upload Files"
                        }
                    }
                }
            }
        }
    }
}

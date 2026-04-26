package com.timothymarias.familyarchive.views.admin

import com.fasterxml.jackson.databind.ObjectMapper
import com.timothymarias.familyarchive.views.AdminViewContext
import com.timothymarias.familyarchive.views.Annotation
import com.timothymarias.familyarchive.components.adminLayout
import com.timothymarias.familyarchive.components.artifactEditForm
import com.timothymarias.familyarchive.components.artifactTypeTable
import com.timothymarias.familyarchive.views.renderHtml
import com.timothymarias.familyarchive.views.Artifact
import com.timothymarias.familyarchive.model.ArtifactType
import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.img
import kotlinx.html.p
import kotlinx.html.span

/**
 * View functions for admin artifact type pages (Photos, Videos, Audio, Documents, Ledgers, Letters, Other).
 * Provides shared view logic for artifact type-specific controllers.
 */
object AdminArtifactTypeViews {
    /**
     * Renders the artifact type index page (list view).
     *
     * @param ctx Admin view context
     * @param artifacts List of artifacts of the specific type
     * @param displayName Plural display name (e.g., "Photos", "Videos")
     * @param routeSegment URL segment (e.g., "photos", "videos")
     */
    fun index(
        ctx: AdminViewContext,
        artifacts: List<Artifact>,
        displayName: String,
        routeSegment: String,
    ): String =
        renderHtml {
            adminLayout(
                title = "$displayName - Admin",
                pageTitle = displayName,
                isDevMode = ctx.isDevMode,
                auth = ctx.auth,
                csrf = ctx.csrf,
                successMessage = ctx.successMessage,
                errorMessage = ctx.errorMessage,
            ) {
                artifactTypeTable(artifacts, displayName, routeSegment, ctx.csrf)
            }
        }

    /**
     * Renders the edit page for a specific artifact.
     *
     * @param ctx Admin view context
     * @param artifact The artifact to edit
     * @param displayName Singular display name (e.g., "Photo", "Video")
     * @param routeSegment URL segment (e.g., "photos", "videos")
     * @param artifactTypes List of all artifact types for the type selector
     */
    fun edit(
        ctx: AdminViewContext,
        artifact: Artifact,
        displayName: String,
        routeSegment: String,
        artifactTypes: List<ArtifactType>,
    ): String =
        renderHtml {
            adminLayout(
                title = "Edit $displayName - Admin",
                pageTitle = "Edit $displayName",
                isDevMode = ctx.isDevMode,
                auth = ctx.auth,
                csrf = ctx.csrf,
                successMessage = ctx.successMessage,
                errorMessage = ctx.errorMessage,
            ) {
                artifactEditForm(artifact, displayName, routeSegment, artifactTypes, ctx.csrf)
            }
        }

    /**
     * Renders the annotations editor page for a specific artifact.
     *
     * @param ctx Admin view context
     * @param artifact The artifact to edit annotations for
     * @param displayName Singular display name (e.g., "Photo", "Video")
     * @param routeSegment URL segment (e.g., "photos", "videos")
     */
    fun annotations(
        ctx: AdminViewContext,
        artifact: Artifact,
        displayName: String,
        routeSegment: String,
        annotationsByFileId: Map<Long, List<Annotation>> = emptyMap(),
    ): String =
        renderHtml {
            // Serialize artifact files to JSON
            val filesData =
                artifact.files.map { file ->
                    val fileAnnotations = annotationsByFileId[file.id] ?: emptyList()
                    mapOf(
                        "id" to file.id,
                        "storagePath" to file.storagePath,
                        "mimeType" to file.mimeType,
                        "fileSequence" to file.fileSequence,
                        "annotations" to
                            fileAnnotations.map { annotation ->
                                mapOf(
                                    "id" to annotation.id,
                                    "annotationText" to annotation.annotationText,
                                    "xCoord" to annotation.xCoord,
                                    "yCoord" to annotation.yCoord,
                                )
                            },
                    )
                }
            val objectMapper = ObjectMapper()
            val filesJson = objectMapper.writeValueAsString(filesData)

            adminLayout(
                title = "Annotations - $displayName - Admin",
                pageTitle = "Annotations",
                isDevMode = ctx.isDevMode,
                auth = ctx.auth,
                csrf = ctx.csrf,
                successMessage = ctx.successMessage,
                errorMessage = ctx.errorMessage,
            ) {
                div {
                    attributes["id"] = "annotations-editor-root"
                    attributes["data-artifact-id"] = artifact.id.toString()
                    attributes["data-artifact-slug"] = artifact.slug
                    attributes["data-artifact-type"] = routeSegment
                    attributes["data-files"] = filesJson
                    attributes["data-csrf-token"] = ctx.csrf.token

                    // Header with back link
                    div(classes = "mb-6 flex items-center justify-between") {
                        div {
                            p(classes = "text-sm text-gray-600 dark:text-gray-400") {
                                +"${artifact.title ?: "Untitled"} - ${artifact.files.size} file(s)"
                            }
                        }
                        a(
                            href = "/admin/artifacts/$routeSegment/${artifact.id}/edit",
                            classes = "text-blue-600 dark:text-blue-400 hover:underline text-sm",
                        ) {
                            +"← Back to Edit"
                        }
                    }

                    // Gallery grid
                    if (artifact.files.isEmpty()) {
                        div(classes = "text-center py-12") {
                            p(classes = "text-gray-600 dark:text-gray-400") {
                                +"No files to annotate"
                            }
                        }
                    } else {
                        div(classes = "grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6") {
                            artifact.files.forEach { file ->
                                div(classes = "relative group cursor-pointer") {
                                    attributes["data-file-id"] = file.id.toString()
                                    attributes["data-image-gallery-item"] = "true"

                                    // Image container
                                    div(
                                        classes = "aspect-square bg-gray-100 dark:bg-gray-700 rounded-lg overflow-hidden border-2 border-gray-200 dark:border-gray-600 hover:border-blue-500 dark:hover:border-blue-400 transition-colors",
                                    ) {
                                        if (file.mimeType.startsWith("image/")) {
                                            img(
                                                src = "/uploads/${file.storagePath}",
                                                alt = "File ${file.fileSequence}",
                                                classes = "w-full h-full object-contain",
                                            )
                                        }
                                    }

                                    // File info overlay
                                    div(
                                        classes = "mt-2 flex items-center justify-between",
                                    ) {
                                        span(classes = "text-sm font-medium text-gray-900 dark:text-white") {
                                            +"File ${file.fileSequence}"
                                        }
                                        span(classes = "text-xs text-gray-500 dark:text-gray-400") {
                                            val fileAnnotations = annotationsByFileId[file.id] ?: emptyList()
                                            +"${fileAnnotations.size} annotation(s)"
                                        }
                                    }

                                    // Hover overlay
                                    div(
                                        classes = "absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-10 transition-opacity rounded-lg flex items-center justify-center",
                                    ) {
                                        button(
                                            classes = "opacity-0 group-hover:opacity-100 transition-opacity bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700",
                                        ) {
                                            +"Edit Annotations"
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

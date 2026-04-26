package com.timothymarias.familyarchive.views.public

import com.timothymarias.familyarchive.views.ViewContext
import com.timothymarias.familyarchive.components.artifactListItem
import com.timothymarias.familyarchive.components.artifacts.photos.artifactDisplayComponent
import com.timothymarias.familyarchive.components.emptyState
import com.timothymarias.familyarchive.components.pageLayout
import com.timothymarias.familyarchive.components.pagination
import com.timothymarias.familyarchive.views.renderHtml
import com.timothymarias.familyarchive.views.Artifact
import com.timothymarias.familyarchive.model.ArtifactType
import kotlinx.html.SVG
import kotlinx.html.div
import kotlinx.html.h1
import com.timothymarias.familyarchive.repository.Page

object ArtifactViews {
    fun index(
        ctx: ViewContext,
        artifacts: Page<Artifact>,
        artifactType: ArtifactType,
        title: String,
        basePath: String,
        emptyIcon: SVG.() -> Unit,
    ): String =
        renderHtml {
            pageLayout(
                title = title,
                isDevMode = ctx.isDevMode,
                auth = ctx.auth,
            ) {
                div(classes = "max-w-3xl mx-auto") {
                    h1(classes = "text-3xl font-bold text-gray-900 dark:text-white mb-8") {
                        +title
                    }

                    // Empty State
                    if (artifacts.isEmpty) {
                        emptyState(
                            title = "No ${title.lowercase()} yet",
                            description = "Check back later for family ${title.lowercase()}.",
                            icon = emptyIcon,
                        )
                    }

                    // Artifact List
                    if (!artifacts.isEmpty) {
                        div(classes = "space-y-4") {
                            artifacts.content.forEach { artifact ->
                                artifactListItem(artifact, basePath)
                            }
                        }
                    }

                    // Pagination
                    pagination(artifacts, "/$basePath")
                }
            }
        }

    fun show(
        ctx: ViewContext,
        artifact: Artifact,
    ): String =
        renderHtml {
            pageLayout(
                title =
                    artifact.title
                        ?: "Untitled ${artifact.artifactType?.name?.lowercase()?.replaceFirstChar { it.uppercase() }}",
                isDevMode = ctx.isDevMode,
                auth = ctx.auth,
            ) {
                div(classes = "max-w-4xl mx-auto") {
                    h1(classes = "text-3xl font-bold text-gray-900 dark:text-white mb-8") {
                        +(
                            artifact.title
                                ?: "Untitled ${artifact.artifactType?.name?.lowercase()?.replaceFirstChar {
                                    it
                                        .uppercase()
                                }}"
                        )
                    }
                    artifactDisplayComponent(artifact, artifact.files)
                }
            }
        }
}

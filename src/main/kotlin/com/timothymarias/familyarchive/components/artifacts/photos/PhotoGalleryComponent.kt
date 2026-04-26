package com.timothymarias.familyarchive.components.artifacts.photos

import com.timothymarias.familyarchive.views.Annotation
import com.timothymarias.familyarchive.views.Artifact
import com.timothymarias.familyarchive.views.ArtifactFile
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlin.collections.forEach

fun FlowContent.photoGalleryComponent(
    artifact: Artifact,
    artifactFiles: List<ArtifactFile>,
    annotationsByFileId: Map<Long, List<Annotation>> = emptyMap(),
) {
    if (artifactFiles.isEmpty()) return

    div(
        classes = "photo-gallery-list flex flex-col gap-8 w-full",
    ) {
        attributes["data-photo-gallery"] = "true"

        artifactFiles.forEach { file ->
            div(classes = "photo-list-item relative w-full") {
                annotatedPhotoComponent(
                    artifact,
                    file,
                    annotationsByFileId[file.id] ?: emptyList(),
                )
            }
        }
    }
}

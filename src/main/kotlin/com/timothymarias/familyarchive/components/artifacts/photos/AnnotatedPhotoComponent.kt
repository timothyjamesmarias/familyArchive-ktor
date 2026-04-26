package com.timothymarias.familyarchive.components.artifacts.photos

import com.fasterxml.jackson.databind.ObjectMapper
import com.timothymarias.familyarchive.views.Annotation
import com.timothymarias.familyarchive.views.Artifact
import com.timothymarias.familyarchive.views.ArtifactFile
import kotlinx.html.FlowContent
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.img

private val objectMapper = ObjectMapper()

fun FlowContent.annotatedPhotoComponent(
    artifact: Artifact,
    artifactFile: ArtifactFile,
    annotations: List<Annotation> = emptyList(),
) {
    val annotationsJson =
        objectMapper.writeValueAsString(
            annotations.map { annotation ->
                mapOf(
                    "id" to annotation.id,
                    "annotationText" to annotation.annotationText,
                    "xCoord" to annotation.xCoord,
                    "yCoord" to annotation.yCoord,
                )
            },
        )

    div(classes = "w-full") {
        div(
            classes = "relative w-full",
        ) {
            attributes["data-annotated-photo"] = "true"
            attributes["data-file-id"] = "${artifactFile.id}"
            attributes["data-annotations"] = annotationsJson

            img(
                src = "/uploads/${artifactFile.storagePath}",
                alt = artifact.title ?: "Photo ${artifactFile.fileSequence}",
                classes = "w-full h-auto block rounded-lg shadow-sm",
            )
        }
    }
}

package com.timothymarias.familyarchive.components.artifacts.photos

import com.timothymarias.familyarchive.views.Artifact
import com.timothymarias.familyarchive.views.ArtifactFile
import com.timothymarias.familyarchive.model.ArtifactType
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.p

fun FlowContent.artifactDisplayComponent(
    artifact: Artifact,
    artifactFiles: List<ArtifactFile>,
) {
    when (artifact.artifactType) {
        ArtifactType.PHOTO -> photoGalleryComponent(artifact, artifactFiles)
        else -> fallbackInfoMessage(artifact)
    }
}

fun FlowContent.fallbackInfoMessage(artifact: Artifact) {
    div {
        p {
            +"No content for ${artifact.title}"
        }
    }
}

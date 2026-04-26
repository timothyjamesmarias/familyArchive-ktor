package com.timothymarias.familyarchive.model

enum class EventType {
    BIRTH,
    DEATH,
    BAPTISM,
    BURIAL,
    MARRIAGE,
    DIVORCE,
    OCCUPATION,
    RESIDENCE,
    EMIGRATION,
    IMMIGRATION,
    NATURALIZATION,
    EDUCATION,
    MILITARY,
    CENSUS,
    CHRISTENING,
    CONFIRMATION,
    ORDINATION,
    ADOPTION,
    OTHER,
}

enum class FamilyRole {
    FATHER,
    MOTHER,
    CHILD,
}

enum class ArtifactType(
    val displayName: String,
    val pluralDisplayName: String,
    val routeSegment: String,
    val componentKey: String,
) {
    PHOTO("Photo", "Photos", "photos", "photo-gallery"),
    LETTER("Letter", "Letters", "letters", "letters"),
    DOCUMENT("Document", "Documents", "documents", "documents"),
    LEDGER("Ledger", "Ledgers", "ledgers", "ledgers"),
    AUDIO("Audio Recording", "Audio", "audio", "audio"),
    VIDEO("Video Recording", "Videos", "videos", "videos"),
    OTHER("Other", "Other", "other", "other"),
    ;

    companion object {
        fun fromString(value: String): ArtifactType =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: OTHER

        fun fromRouteSegment(segment: String): ArtifactType =
            entries.find { it.routeSegment.equals(segment, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown artifact type: $segment")
    }
}

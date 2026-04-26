package com.timothymarias.familyarchive.database

import com.timothymarias.familyarchive.model.ArtifactType
import com.timothymarias.familyarchive.model.EventType
import com.timothymarias.familyarchive.model.FamilyRole
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.json.jsonb
import java.math.BigDecimal
import java.time.LocalDateTime

private val jsonFormat = Json { ignoreUnknownKeys = true }

private fun Table.jsonbNullable(name: String): Column<JsonElement?> =
    jsonb<JsonElement>(name, { jsonFormat.encodeToString(JsonElement.serializer(), it) }, { jsonFormat.decodeFromString(JsonElement.serializer(), it) }).nullable()

// ---------------------------------------------------------------------------
// Tier 1: No foreign keys
// ---------------------------------------------------------------------------

object Users : LongIdTable("users") {
    val email = varchar("email", 255).uniqueIndex()
    val password = varchar("password", 255)
    val name = varchar("name", 255)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

object Places : LongIdTable("places") {
    val name = varchar("name", 500)
    val normalizedName = varchar("normalized_name", 500).nullable()
    val city = varchar("city", 255).nullable()
    val stateProvince = varchar("state_province", 255).nullable()
    val country = varchar("country", 255).nullable()
    val latitude = decimal("latitude", 10, 8).nullable()
    val longitude = decimal("longitude", 11, 8).nullable()
    val gedcomRawData = jsonbNullable("gedcom_raw_data")
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

// ---------------------------------------------------------------------------
// Tier 2: Simple foreign keys
// ---------------------------------------------------------------------------

object Individuals : LongIdTable("individuals") {
    val gedcomId = varchar("gedcom_id", 50).uniqueIndex().nullable()
    val givenName = varchar("given_name", 255).nullable()
    val surname = varchar("surname", 255).nullable()
    val sex = char("sex").nullable()
    val isTreeRoot = bool("is_tree_root").default(false)
    val gedcomRawData = jsonbNullable("gedcom_raw_data")
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
    val lastImportedAt = datetime("last_imported_at").nullable()
    val deletedAt = datetime("deleted_at").nullable()
}

object Families : LongIdTable("families") {
    val gedcomId = varchar("gedcom_id", 50).uniqueIndex().nullable()
    val marriageDateString = varchar("marriage_date_string", 255).nullable()
    val marriageDateParsed = datetime("marriage_date_parsed").nullable()
    val marriagePlaceId = reference("marriage_place_id", Places).nullable()
    val divorceDateString = varchar("divorce_date_string", 255).nullable()
    val divorceDateParsed = datetime("divorce_date_parsed").nullable()
    val gedcomRawData = jsonbNullable("gedcom_raw_data")
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
    val lastImportedAt = datetime("last_imported_at").nullable()
    val deletedAt = datetime("deleted_at").nullable()
}

object Artifacts : LongIdTable("artifacts") {
    val slug = varchar("slug", 255).uniqueIndex()
    val artifactType = enumerationByName<ArtifactType>("artifact_type", 50).default(ArtifactType.DOCUMENT)
    val title = varchar("title", 500).nullable()
    val storagePath = varchar("storage_path", 1000)
    val mimeType = varchar("mime_type", 255)
    val fileSize = long("file_size")
    val uploadedAt = datetime("uploaded_at").clientDefault { LocalDateTime.now() }
    val originalDateString = varchar("original_date_string", 255).nullable()
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

object Articles : LongIdTable("articles") {
    val slug = varchar("slug", 255).uniqueIndex()
    val title = varchar("title", 500)
    val excerpt = varchar("excerpt", 1000).nullable()
    val content = text("content").default("")
    val publishedAt = datetime("published_at").nullable()
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

// ---------------------------------------------------------------------------
// Tier 3: Join tables and child entities
// ---------------------------------------------------------------------------

object FamilyMembers : Table("family_members") {
    val familyId = reference("family_id", Families)
    val individualId = reference("individual_id", Individuals)
    val role = enumerationByName<FamilyRole>("role", 20)
    val childOrder = integer("child_order").nullable()
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val deletedAt = datetime("deleted_at").nullable()

    override val primaryKey = PrimaryKey(familyId, individualId, role)
}

object ArtifactFiles : LongIdTable("artifact_files") {
    val artifactId = reference("artifact_id", Artifacts)
    val fileSequence = integer("file_sequence")
    val storagePath = varchar("storage_path", 1000)
    val mimeType = varchar("mime_type", 255)
    val fileSize = long("file_size")
    val thumbnailPath = varchar("thumbnail_path", 1000).nullable()
    val thumbnailSize = varchar("thumbnail_size", 50).nullable()
    val uploadedAt = datetime("uploaded_at").clientDefault { LocalDateTime.now() }
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

object IndividualEvents : LongIdTable("individual_events") {
    val individualId = reference("individual_id", Individuals)
    val eventType = enumerationByName<EventType>("event_type", 50)
    val dateString = varchar("date_string", 255).nullable()
    val dateParsed = datetime("date_parsed").nullable()
    val placeId = reference("place_id", Places).nullable()
    val description = text("description").nullable()
    val gedcomRawData = jsonbNullable("gedcom_raw_data")
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

object Transcriptions : LongIdTable("transcriptions") {
    val artifactId = reference("artifact_id", Artifacts)
    val transcriptionText = text("transcription_text")
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

object Translations : LongIdTable("translations") {
    val transcriptionId = reference("transcription_id", Transcriptions)
    val translatedText = text("translated_text")
    val targetLanguage = varchar("target_language", 10)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

object Commentaries : LongIdTable("commentaries") {
    val artifactId = reference("artifact_id", Artifacts)
    val commentaryText = text("commentary_text")
    val commentaryType = varchar("commentary_type", 50).nullable()
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

object Annotations : LongIdTable("annotations") {
    val artifactFileId = reference("artifact_file_id", ArtifactFiles)
    val annotationText = text("annotation_text")
    val xCoord = decimal("x_coord", 10, 6).nullable()
    val yCoord = decimal("y_coord", 10, 6).nullable()
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

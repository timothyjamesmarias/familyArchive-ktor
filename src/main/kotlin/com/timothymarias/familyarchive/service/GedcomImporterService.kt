package com.timothymarias.familyarchive.service

import com.timothymarias.familyarchive.model.EventType
import com.timothymarias.familyarchive.model.FamilyRole
import com.timothymarias.familyarchive.repository.FamilyMemberRepository
import com.timothymarias.familyarchive.repository.FamilyRepository
import com.timothymarias.familyarchive.repository.IndividualEventRepository
import com.timothymarias.familyarchive.repository.IndividualRepository
import com.timothymarias.familyarchive.repository.PlaceRepository
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.gedcom4j.model.Gedcom
import org.gedcom4j.model.Individual
import org.gedcom4j.parser.GedcomParser
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

data class ImportResult(
    val success: Boolean,
    val individualsImported: Int = 0,
    val familiesImported: Int = 0,
    val eventsImported: Int = 0,
    val placesImported: Int = 0,
    val warnings: List<String> = emptyList(),
    val errors: List<String> = emptyList(),
)

class GedcomImporterService(
    private val individualRepository: IndividualRepository,
    private val familyRepository: FamilyRepository,
    private val familyMemberRepository: FamilyMemberRepository,
    private val placeRepository: PlaceRepository,
    private val individualEventRepository: IndividualEventRepository,
) {
    private val logger = LoggerFactory.getLogger(GedcomImporterService::class.java)

    fun importFile(file: File): ImportResult {
        try {
            if (!file.exists()) {
                return ImportResult(success = false, errors = listOf("File not found: ${file.absolutePath}"))
            }

            logger.info("Parsing GEDCOM file: ${file.name}")
            val parser = GedcomParser()
            parser.load(file.absolutePath)
            val gedcom = parser.gedcom

            val warnings = parser.errors.map { it.toString() } + parser.warnings.map { it.toString() }
            if (warnings.isNotEmpty()) logger.warn("GEDCOM parser warnings: $warnings")

            logger.info("Starting GEDCOM import...")

            return transaction {
                val placesCreated = importPlaces(gedcom)
                val individualsCreated = importIndividuals(gedcom)
                val familiesCreated = importFamilies(gedcom)
                val eventsCreated = importIndividualEvents(gedcom)

                logger.info("Import completed successfully")

                ImportResult(
                    success = true,
                    individualsImported = individualsCreated,
                    familiesImported = familiesCreated,
                    eventsImported = eventsCreated,
                    placesImported = placesCreated,
                    warnings = warnings,
                )
            }
        } catch (e: Exception) {
            logger.error("Error importing GEDCOM file", e)
            return ImportResult(success = false, errors = listOf("Import failed: ${e.message}"))
        }
    }

    private fun importPlaces(gedcom: Gedcom): Int {
        val placeNames = mutableSetOf<String>()

        gedcom.individuals.values.forEach { individual ->
            individual.events?.forEach { event ->
                event.place?.placeName?.let { placeNames.add(it) }
            }
        }
        gedcom.families.values.forEach { family ->
            family.events?.forEach { event ->
                event.place?.placeName?.let { placeNames.add(it) }
            }
        }

        var created = 0
        placeNames.forEach { placeName ->
            val normalizedName = placeName.trim().lowercase()
            if (placeRepository.findByNormalizedName(normalizedName) == null) {
                placeRepository.create(placeName, normalizedName, null, null, null, null, null, null)
                created++
            }
        }

        logger.info("Imported $created places")
        return created
    }

    private fun importIndividuals(gedcom: Gedcom): Int {
        var created = 0

        gedcom.individuals.forEach { (xref, gedcomIndividual) ->
            val existing = individualRepository.findByGedcomId(xref)

            if (existing == null) {
                val givenName = gedcomIndividual.names?.firstOrNull()?.givenName?.value
                val surname = gedcomIndividual.names?.firstOrNull()?.surname?.value
                val sex = gedcomIndividual.sex?.value?.firstOrNull()

                individualRepository.create(
                    gedcomId = xref,
                    givenName = givenName,
                    surname = surname,
                    sex = sex,
                    isTreeRoot = false,
                    gedcomRawData = convertToJsonElement(gedcomIndividual),
                )
                created++
            } else {
                val givenName = gedcomIndividual.names?.firstOrNull()?.givenName?.value
                val surname = gedcomIndividual.names?.firstOrNull()?.surname?.value
                val sex = gedcomIndividual.sex?.value?.firstOrNull()

                individualRepository.update(
                    id = existing.id,
                    gedcomId = xref,
                    givenName = givenName,
                    surname = surname,
                    sex = sex,
                    isTreeRoot = existing.isTreeRoot,
                    lastImportedAt = LocalDateTime.now(),
                )
            }
        }

        logger.info("Imported $created individuals")
        return gedcom.individuals.size
    }

    private fun importFamilies(gedcom: Gedcom): Int {
        var created = 0

        gedcom.families.forEach { (xref, gedcomFamily) ->
            val existing = familyRepository.findByGedcomId(xref)
            val marriageEvent = gedcomFamily.events?.firstOrNull { it.type.tag == "MARR" }
            val marriageDateString = marriageEvent?.date?.value
            val marriageDateParsed = parseDate(marriageDateString)

            var marriagePlaceId: Long? = null
            marriageEvent?.place?.placeName?.let { placeName ->
                val normalizedName = placeName.trim().lowercase()
                marriagePlaceId = placeRepository.findByNormalizedName(normalizedName)?.id
            }

            val familyId = if (existing == null) {
                created++
                familyRepository.create(
                    gedcomId = xref,
                    marriageDateString = marriageDateString,
                    marriageDateParsed = marriageDateParsed,
                    marriagePlaceId = marriagePlaceId,
                    divorceDateString = null,
                    divorceDateParsed = null,
                    gedcomRawData = convertToJsonElement(gedcomFamily),
                )
            } else {
                familyRepository.update(
                    id = existing.id,
                    marriageDateString = marriageDateString,
                    marriageDateParsed = marriageDateParsed,
                    marriagePlaceId = marriagePlaceId,
                    divorceDateString = existing.divorceDateString,
                    divorceDateParsed = existing.divorceDateParsed,
                )
                existing.id
            }

            createFamilyMemberRelationships(familyId, gedcomFamily, gedcom)
        }

        logger.info("Imported $created families")
        return gedcom.families.size
    }

    private fun findXrefForIndividual(gedcom: Gedcom, individual: Individual?): String? {
        if (individual == null) return null
        return gedcom.individuals.entries.find { it.value == individual }?.key
    }

    private fun createFamilyMemberRelationships(
        familyId: Long,
        gedcomFamily: org.gedcom4j.model.Family,
        gedcom: Gedcom,
    ) {
        familyMemberRepository.hardDeleteByFamilyId(familyId)

        findXrefForIndividual(gedcom, gedcomFamily.husband?.individual)?.let { xref ->
            individualRepository.findByGedcomId(xref)?.let { individual ->
                familyMemberRepository.create(familyId, individual.id, FamilyRole.FATHER)
            }
        }

        findXrefForIndividual(gedcom, gedcomFamily.wife?.individual)?.let { xref ->
            individualRepository.findByGedcomId(xref)?.let { individual ->
                familyMemberRepository.create(familyId, individual.id, FamilyRole.MOTHER)
            }
        }

        gedcomFamily.children?.forEachIndexed { index, child ->
            findXrefForIndividual(gedcom, child.individual)?.let { xref ->
                individualRepository.findByGedcomId(xref)?.let { individual ->
                    familyMemberRepository.create(familyId, individual.id, FamilyRole.CHILD, index)
                }
            }
        }
    }

    private fun importIndividualEvents(gedcom: Gedcom): Int {
        var eventsCreated = 0

        gedcom.individuals.forEach { (xref, gedcomIndividual) ->
            val individual = individualRepository.findByGedcomId(xref) ?: return@forEach

            individualEventRepository.deleteByIndividualId(individual.id)

            gedcomIndividual.events?.forEach { gedcomEvent ->
                val eventType = mapEventType(gedcomEvent.type.tag)
                var placeId: Long? = null
                gedcomEvent.place?.placeName?.let { placeName ->
                    val normalizedName = placeName.trim().lowercase()
                    placeId = placeRepository.findByNormalizedName(normalizedName)?.id
                }

                individualEventRepository.create(
                    individualId = individual.id,
                    eventType = eventType,
                    dateString = gedcomEvent.date?.value,
                    dateParsed = parseDate(gedcomEvent.date?.value),
                    placeId = placeId,
                    description = gedcomEvent.description?.value,
                    gedcomRawData = convertToJsonElement(gedcomEvent),
                )
                eventsCreated++
            }
        }

        logger.info("Imported $eventsCreated events")
        return eventsCreated
    }

    private fun mapEventType(tag: String): EventType =
        when (tag) {
            "BIRT" -> EventType.BIRTH
            "DEAT" -> EventType.DEATH
            "BAPM", "BAPL", "CHR", "CHRA" -> EventType.BAPTISM
            "BURI" -> EventType.BURIAL
            "MARR" -> EventType.MARRIAGE
            "DIV" -> EventType.DIVORCE
            "OCCU" -> EventType.OCCUPATION
            "RESI" -> EventType.RESIDENCE
            "EMIG" -> EventType.EMIGRATION
            "IMMI" -> EventType.IMMIGRATION
            "NATU" -> EventType.NATURALIZATION
            "EDUC" -> EventType.EDUCATION
            "_MILT", "EVEN" -> EventType.MILITARY
            "CENS" -> EventType.CENSUS
            "CONF" -> EventType.CONFIRMATION
            "ORDN" -> EventType.ORDINATION
            "ADOP" -> EventType.ADOPTION
            else -> EventType.OTHER
        }

    private fun parseDate(dateString: String?): LocalDateTime? {
        if (dateString.isNullOrBlank()) return null

        val formats = listOf("d MMM yyyy", "MMM yyyy", "yyyy")
        for (format in formats) {
            try {
                val formatter = DateTimeFormatter.ofPattern(format, java.util.Locale.ENGLISH)
                val temporal = formatter.parse(dateString.trim())
                val year = if (temporal.isSupported(java.time.temporal.ChronoField.YEAR)) {
                    temporal.get(java.time.temporal.ChronoField.YEAR)
                } else {
                    return null
                }
                val month = if (temporal.isSupported(java.time.temporal.ChronoField.MONTH_OF_YEAR)) {
                    temporal.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)
                } else {
                    1
                }
                val day = if (temporal.isSupported(java.time.temporal.ChronoField.DAY_OF_MONTH)) {
                    temporal.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
                } else {
                    1
                }
                return LocalDateTime.of(year, month, day, 0, 0)
            } catch (_: DateTimeParseException) {
            }
        }

        logger.debug("Could not parse date: $dateString")
        return null
    }

    private fun convertToJsonElement(obj: Any): JsonElement =
        JsonObject(
            mapOf(
                "type" to JsonPrimitive(obj.javaClass.simpleName),
                "toString" to JsonPrimitive(obj.toString()),
            ),
        )
}

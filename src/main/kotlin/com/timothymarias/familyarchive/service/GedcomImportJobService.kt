package com.timothymarias.familyarchive.service

import org.jobrunr.jobs.JobId
import org.jobrunr.jobs.annotations.Job
import org.jobrunr.scheduling.JobScheduler
import org.slf4j.LoggerFactory
import java.io.File

class GedcomImportJobService(
    private val jobScheduler: JobScheduler,
    private val gedcomImporterService: GedcomImporterService,
) {
    private val logger = LoggerFactory.getLogger(GedcomImportJobService::class.java)

    fun enqueueImport(filePath: String): JobId {
        logger.info("Enqueuing GEDCOM import job for file: $filePath")
        val jobId = jobScheduler.enqueue<GedcomImportJobService> { performImport(filePath) }
        logger.info("GEDCOM import job enqueued with ID: $jobId")
        return jobId
    }

    @Job(name = "Import GEDCOM file: %0")
    fun performImport(filePath: String): ImportResult {
        logger.info("Starting GEDCOM import for file: $filePath")
        val file = File(filePath)
        val result = gedcomImporterService.importFile(file)

        if (result.success) {
            logger.info(
                "GEDCOM import completed: ${result.individualsImported} individuals, " +
                    "${result.familiesImported} families, ${result.eventsImported} events, ${result.placesImported} places",
            )
        } else {
            logger.error("GEDCOM import failed: ${result.errors}")
        }

        return result
    }
}

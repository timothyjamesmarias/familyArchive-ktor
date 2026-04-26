package com.timothymarias.familyarchive.service

import com.timothymarias.familyarchive.model.EventType
import com.timothymarias.familyarchive.repository.IndividualEventRecord
import com.timothymarias.familyarchive.repository.IndividualEventRepository
import org.jetbrains.exposed.sql.transactions.transaction

class IndividualEventService(
    private val individualEventRepository: IndividualEventRepository,
) {
    fun findById(id: Long): IndividualEventRecord? = transaction { individualEventRepository.findById(id) }

    fun findByIndividualId(individualId: Long): List<IndividualEventRecord> = transaction {
        individualEventRepository.findByIndividualId(individualId)
    }

    fun findByIndividualIdOrderByDate(individualId: Long): List<IndividualEventRecord> = transaction {
        individualEventRepository.findByIndividualIdOrderByDate(individualId)
    }

    fun findByIndividualIdAndEventType(individualId: Long, eventType: EventType): List<IndividualEventRecord> = transaction {
        individualEventRepository.findByIndividualIdAndEventType(individualId, eventType)
    }

    fun findByPlaceId(placeId: Long): List<IndividualEventRecord> = transaction {
        individualEventRepository.findByPlaceId(placeId)
    }

    fun delete(id: Long) = transaction { individualEventRepository.delete(id) }
}

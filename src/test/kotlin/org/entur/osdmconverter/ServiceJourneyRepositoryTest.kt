package org.entur.osdmconverter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ServiceJourneyRepositoryTest {

    private lateinit var serviceJourneyRepository: ServiceJourneyRepository
    @BeforeEach
    fun setUp() {
        serviceJourneyRepository = ServiceJourneyRepository()
        serviceJourneyRepository.readFile("trip-patterns.json")
    }

    @Test
    fun canLoadLargeFile() {
        assertEquals(31, serviceJourneyRepository.serviceJourneys.size)
    }

    @Test
    fun name() {
        assertNotNull(serviceJourneyRepository.getServiceJourney("SE:050:ServiceJourney:747400000000001085"))
    }
}

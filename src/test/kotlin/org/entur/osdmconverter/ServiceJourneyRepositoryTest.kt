package org.entur.osdmconverter

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ServiceJourneyRepositoryTest {

    @Test
    fun name() {
        assertNotNull(ServiceJourneyRepository().getServiceJourney("SKY:ServiceJourney:1-174956-16324788"))
    }
}

package org.entur.osdmconverter

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.entur.osdmconverter.client.journeyplanner.JourneyPlannerResponse
import org.entur.osdmconverter.client.journeyplanner.ServiceJourney
import org.springframework.stereotype.Component

@Component
class ServiceJourneyRepository {
    val serviceJourneys = run {
        val response: JourneyPlannerResponse = ObjectMapper()
            .registerModule(KotlinModule.Builder().build())
            .registerModule(JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

            .readValue(
                javaClass.classLoader.getResourceAsStream("trip-patterns.json").reader()
            )
        val serviceJourneys = response.data.trip.tripPatterns
            .flatMap { it.legs }
            .mapNotNull { it.serviceJourney }

        val result = HashMap<String, ServiceJourney>()
        for (serviceJourney in serviceJourneys) {
            result[serviceJourney.id] = serviceJourney
        }
        result
    }

    fun getServiceJourney(id: String): ServiceJourney? {
        return serviceJourneys[id]
    }
}

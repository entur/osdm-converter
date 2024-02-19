package org.entur.osdmconverter

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.entur.osdmconverter.client.journeyplanner.JourneyPlannerResponse
import org.entur.osdmconverter.client.journeyplanner.ServiceJourney


class ServiceJourneyRepository {
    val serviceJourneys = HashMap<String, ServiceJourney>()
    fun readFile(filename: String) {
        val response: JourneyPlannerResponse = ObjectMapper()
            .registerModule(KotlinModule.Builder().build())
            .registerModule(JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

            .readValue(
                javaClass.classLoader.getResourceAsStream(filename).reader()
            )
        val serviceJourneyList = response.data.trip.tripPatterns
            .flatMap { it.legs }
            .mapNotNull { it.serviceJourney }

        for (serviceJourney in serviceJourneyList) {
            serviceJourneys[serviceJourney.id] = serviceJourney
        }
    }

    fun getServiceJourney(id: String): ServiceJourney? {
        return serviceJourneys[id]
    }

    fun addServiceJourney(id: String) : ServiceJourney {
        val serviceJourney = ServiceJourney(
            id = id,
            transportMode = "rail",
            transportSubmode = "highSpeed",
            line = ServiceJourney.Line("lineid", null, null, null),
            keyValuesList = mutableListOf()
        )
        serviceJourneys[id] = serviceJourney
        return serviceJourney
    }
}

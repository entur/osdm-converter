package org.entur.osdmconverter.client.journeyplanner

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalTime

data class JourneyPlannerResponse(val data: Data) {
    data class Data(val trip: Trip) {
    }

    data class Trip(val tripPatterns: List<TripPattern>) {
    }

    data class TripPattern(val legs: List<Leg>) {
    }

    data class Leg(val serviceJourney: ServiceJourney?, val mode: String)
}

data class ServiceJourney(
    val id: String,
    val transportMode: String,
    val transportSubmode: String,
    val line: Line,
    val passingTimes: List<PassingTime>,
    @JsonProperty("keyValues")
    val keyValuesList: List<KeyValue>?
) {
    val customKeyValues: MutableMap<String, String> = HashMap()

    data class Line(
        val id: String,
        val publicCode: String?,
        val authority: IdName?,
        val operator: IdName?
    )

    data class IdName(val id: String, val name: String)
    data class PassingTime(val quay: Quay, val departure: Time, val arrival: Time)
    data class Quay(val stopPlace: IdName)
    data class Time(val time: LocalTime, val dayOffset: Int)

    data class KeyValue(val key: String, val value: String)

    fun getPassingTime(stopPlaceId: String): PassingTime? {
        return passingTimes.find { it.quay.stopPlace.id == stopPlaceId }
    }

    init {
        if (null != keyValuesList) for (keyValue in keyValuesList) {
            customKeyValues[keyValue.key] = keyValue.value
        }
    }
}

package org.entur.osdmconverter.client.journeyplanner

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalTime

data class JourneyPlannerResponse(val data: Data) {
    data class Data(val trip: Trip)
    data class Trip(val tripPatterns: List<TripPattern>)
    data class TripPattern(val legs: List<Leg>)
    data class Leg(val serviceJourney: ServiceJourney?, val mode: String)
}

data class ServiceJourney(
    val id: String,
    val transportMode: String,
    val transportSubmode: String,
    val line: Line,
    val passingTimes: MutableList<PassingTime> = ArrayList(),
    @JsonProperty("keyValues")
    val keyValuesList: MutableList<KeyValue> = ArrayList()
) {
    data class Line(
        val id: String,
        val publicCode: String?,
        val authority: IdName?,
        val operator: IdName?,
        @JsonProperty("keyValues")
        val keyValuesList: MutableList<KeyValue> = ArrayList()
    )

    data class IdName(val id: String, val name: String)
    data class PassingTime(val quay: Quay, val departure: Time, val arrival: Time)
    data class Quay(val stopPlace: IdName)
    data class Time(val time: LocalTime, val dayOffset: Int)

    data class KeyValue(val key: String, val value: String)

    fun getPassingTime(stopPlaceId: String): PassingTime? {
        return passingTimes.find { it.quay.stopPlace.id == stopPlaceId }
    }

    fun addPassingTime(arrival: LocalTime, departure: LocalTime, stopPlaceId: String) {
        passingTimes.add(
            PassingTime(
                arrival = Time(arrival, 0),
                departure = Time(departure, 0),
                quay = Quay(IdName(stopPlaceId, "test"))
            )
        )
    }

    fun getValue(key: String): String? {
        return keyValuesList.find { it.key == key }?.value
    }

    fun setValue(key: String, value: String) {
        keyValuesList.add(KeyValue(key, value))
    }

    fun setLineValue(key:String, value: String) {
        line.keyValuesList.add(KeyValue(key, value))
    }

    fun getLineValue(key: String): String? {
        return line.keyValuesList.find { it.key == key }?.value
    }
}

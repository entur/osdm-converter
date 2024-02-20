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
    var transportMode: String,
    val transportSubmode: String,
    val line: Line,
    val passingTimes: MutableList<PassingTime> = ArrayList(),
    @JsonProperty("keyValues")
    val keyValuesList: MutableList<KeyValue> = ArrayList(),
    val trainNumbers: MutableList<TrainNumber> = ArrayList()
) {
    data class Line(
        val id: String,
        var publicCode: String?,
        val authority: IdName?,
        val operator: IdName?,
        @JsonProperty("keyValues")
        val keyValuesList: MutableList<KeyValue> = ArrayList()

    ) {
        fun setValue(key: String, value: String) {
            keyValuesList.add(KeyValue(key, value))
        }

        fun getValue(key: String): String? {
            return keyValuesList.find { it.key == key }?.value
        }
    }

    data class IdName(val id: String, val name: String)
    data class PassingTime(val quay: Quay, val departure: Time, val arrival: Time)
    data class Quay(val stopPlace: IdName)
    data class Time(val time: LocalTime, val dayOffset: Int)

    data class KeyValue(val key: String, val value: String)

    data class TrainNumber (val id: String, val forAdvertisement: String)

    fun getPassingTime(stopPlaceId: String): PassingTime? {
        return passingTimes.find { it.quay.stopPlace.id == stopPlaceId }
    }

    fun addPassingTime(stopPlaceId: String, arrival: String, departure: String) {
        addPassingTime(stopPlaceId, LocalTime.parse(arrival), LocalTime.parse(departure))
    }

    fun addPassingTime(stopPlaceId: String, arrival: LocalTime, departure: LocalTime) {
        passingTimes.removeIf { it.quay.stopPlace.id == stopPlaceId }
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

    fun addTrainNumber(forAdvertisement: String) {
        trainNumbers.add(TrainNumber("SE:050:TrainNumber:9011686028800000_$forAdvertisement", forAdvertisement))
    }
}

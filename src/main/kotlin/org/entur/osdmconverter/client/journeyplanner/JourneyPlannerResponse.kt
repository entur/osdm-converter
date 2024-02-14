package org.entur.osdmconverter.client.journeyplanner

import java.time.LocalTime

data class GetServiceJourneyResponse(val data: Data) {
    data class Data(val serviceJourney: ServiceJourney)
}

data class ServiceJourney(
    val id: String,
    val transportMode: String,
    val transportSubmode: String,
    val line: Line,
    val passingTimes: List<PassingTime>
) {
    data class Line(
        val id: String,
        val publicCode: String,
        val authority: IdName,
        val operator: IdName
    )

    data class IdName(val id: String, val name: String)
    data class PassingTime(val quay: Quay, val departure: Time, val arrival: Time)
    data class Quay(val stopPlace: IdName)
    data class Time(val time: LocalTime, val dayOffset: Int)

    fun getPassingTime(stopPlaceId: String): PassingTime? {
        return passingTimes.find { it.quay.stopPlace.id == stopPlaceId }
    }
}

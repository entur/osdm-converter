package org.entur.osdmconverter

import java.time.LocalDate

data class ConvertTripPatternRequest(val id: String?, val legs: List<Leg>) {
    data class Leg(
        val id: String?,
        val travelDate: LocalDate,
        val fromStopPlaceId: String,
        val toStopPlaceId: String,
        val serviceJourneyId: String
    )
}

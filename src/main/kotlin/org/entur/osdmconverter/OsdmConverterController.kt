package org.entur.osdmconverter

import io.osdm.*
import org.entur.osdmconverter.client.journeyplanner.ServiceJourney
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.ZoneId
import java.time.ZonedDateTime

@RestController
class OsdmConverterController(
    private val stopsRepository: StopsRepository,
    private val serviceJourneyRepository: ServiceJourneyRepository
) {
    val modes = mapOf(
        "bus" to "BUS",
        "water" to "SHIP",
        "rail" to "TRAIN",
        "tram" to "TRAM",
        "taxi" to "SHARED_TAXI",
        "metro" to "UNDERGROUND"
    )

    @PostMapping("trip-pattern")
    fun convertTripPattern(@RequestBody request: ConvertTripPatternRequest): ConvertTripPatternResponse {
        return ConvertTripPatternResponse(
            request.id,
            TripSpecification(legs = request.legs.map(::toTripLegSpecification))
        )
    }

    private fun toTripLegSpecification(leg: ConvertTripPatternRequest.Leg): TripLegSpecification {
        return TripLegSpecification(timedLeg = toTimedLegSpecification(leg))
    }

    private fun toTimedLegSpecification(leg: ConvertTripPatternRequest.Leg): TimedLegSpecification {
        val serviceJourney = serviceJourneyRepository.getServiceJourney(leg.serviceJourneyId)
            ?: throw IllegalArgumentException("Unknown service journey: " + leg.serviceJourneyId)

        return TimedLegSpecification(
            start = toBoardSpecification(leg, serviceJourney),
            end = toAlightSpecification(leg, serviceJourney),
            service = toDatedJourney(serviceJourney)
        )
    }

    private fun toDatedJourney(serviceJourney: ServiceJourney): DatedJourney {
        return DatedJourney(
            mode = toMode(serviceJourney.transportMode),
            vehicleNumbers = serviceJourney.line.publicCode?.let { listOf(it) } ?: emptyList(),
            carriers = serviceJourney.line.getValue("salesAuthorityNumber")?.let { listOf(toCarrier(it)) } ?: emptyList(),
            productCategory = toProductCategory(serviceJourney.getValue("productCode"))
        )
    }

    private fun toCarrier(salesAuthorityNumber: String): NamedCompany {
        return NamedCompany(
            name = null,
            ref = "urn:x_swe:carrier:$salesAuthorityNumber"
        )
    }

    private fun toProductCategory(productCode: String?): ProductCategory? {
        if (productCode == null) return null
        return ProductCategory(
            name = "",
            shortName = "",
            productCategoryRef = "urn:x_swe:sbc:$productCode"
        )
    }

    private fun toMode(transportMode: String): Mode {
        return Mode(ptMode = modes[transportMode] ?: "")
    }

    private fun toAlightSpecification(
        leg: ConvertTripPatternRequest.Leg,
        serviceJourney: ServiceJourney
    ): AlightSpecification {
        val passingTime = serviceJourney.getPassingTime(leg.toStopPlaceId)
            ?: throw IllegalArgumentException("This service journey does not pass station " + leg.toStopPlaceId)

        return AlightSpecification(
            stopPlaceRef = StopPlaceRef(getRikshallplatsUrn(leg.toStopPlaceId), objectType = "StopPlaceRef"),
            serviceArrival = ServiceTime(
                timetabledTime = ZonedDateTime.of(
                    leg.travelDate,
                    passingTime.arrival.time,
                    ZoneId.of("Europe/Stockholm")
                )
            )
        )
    }

    private fun toBoardSpecification(
        leg: ConvertTripPatternRequest.Leg,
        serviceJourney: ServiceJourney
    ): BoardSpecification {
        val passingTime = serviceJourney.getPassingTime(leg.fromStopPlaceId)
            ?: throw IllegalArgumentException("This service journey does not pass station " + leg.fromStopPlaceId)

        return BoardSpecification(
            stopPlaceRef = StopPlaceRef(getRikshallplatsUrn(leg.fromStopPlaceId), objectType = "StopPlaceRef"),
            serviceDeparture = ServiceTime(
                timetabledTime = ZonedDateTime.of(
                    leg.travelDate,
                    passingTime.departure.time,
                    ZoneId.of("Europe/Stockholm")
                )
            )
        )
    }

    private fun getRikshallplatsUrn(stopPlaceId: String): String {
        val rikshallPlatsNr = stopsRepository.getRikshallplatsNr(stopPlaceId)
        if (rikshallPlatsNr == null) throw RuntimeException("RikshallPlats number not found")

        return "urn:x_swe:stn:$rikshallPlatsNr"
    }
}


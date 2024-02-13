package org.entur.osdmconverter

import io.osdm.*
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.ZonedDateTime

@RestController
class OsdmConverterController(
    private val stopsRepository: StopsRepository
) {

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
        return TimedLegSpecification(
            start = toBoardSpecification(leg),
            end = toAlightSpecification(leg),
            service = toDatedJourney(leg.serviceJourneyId)
        )
    }

    private fun toDatedJourney(serviceJourneyId: String): DatedJourney {
        return DatedJourney(
            mode = Mode(ptMode = "train"),
            vehicleNumbers = listOf(),
            carriers = listOf()
        )
    }

    private fun toAlightSpecification(leg: ConvertTripPatternRequest.Leg): AlightSpecification {
        return AlightSpecification(
            stopPlaceRef = StopPlaceRef(getRikshallplatsUrn(leg.fromStopPlaceId), objectType = "StopPlaceRef"),
            serviceArrival = ServiceTime(timetabledTime = ZonedDateTime.now())
        )
    }

    private fun toBoardSpecification(leg: ConvertTripPatternRequest.Leg): BoardSpecification {
        return BoardSpecification(
            stopPlaceRef = StopPlaceRef(getRikshallplatsUrn(leg.toStopPlaceId), objectType = "StopPlaceRef"),
            serviceDeparture = ServiceTime(timetabledTime = ZonedDateTime.now())
        )
    }

    private fun getRikshallplatsUrn(stopPlaceId: String): String {
        val rikshallPlatsNr = stopsRepository.getRikshallplatsNr(stopPlaceId)
        if(rikshallPlatsNr == null) throw RuntimeException("RikshallPlats number not found")
        return "urn:x_swe:stn:$rikshallPlatsNr"
    }
}


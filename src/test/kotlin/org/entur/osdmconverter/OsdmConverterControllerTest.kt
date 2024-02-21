package org.entur.osdmconverter

import io.osdm.ConvertTripPatternRequest
import io.osdm.Leg
import org.entur.osdmconverter.client.journeyplanner.ServiceJourney
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime

class OsdmConverterControllerTest {
    private lateinit var stopsRepository: StopsRepository
    private lateinit var osdmConverterController: OsdmConverterController
    private lateinit var serviceJourneyRepository: ServiceJourneyRepository
    private lateinit var serviceJourney: ServiceJourney
    private lateinit var request: ConvertTripPatternRequest

    private val fromStopPlaceId = "SE:050:StopPlace:9021050000003000_2"
    private val toStopPlaceId = "SE:050:StopPlace:9021050025315000_2"
    private val travelDate = LocalDate.parse("2024-05-01")

    @BeforeEach
    fun setUp() {
        stopsRepository = StopsRepository()
        serviceJourneyRepository = ServiceJourneyRepository()
        osdmConverterController = OsdmConverterController(stopsRepository, serviceJourneyRepository)

        stopsRepository.addStop(fromStopPlaceId, 6000001)
        stopsRepository.addStop(toStopPlaceId, 6000002)

        serviceJourney = serviceJourneyRepository.addServiceJourney("SE:050:ServiceJourney:121120000338664260")
        serviceJourney.addPassingTime(fromStopPlaceId, LocalTime.now(), LocalTime.now())
        serviceJourney.addPassingTime(toStopPlaceId, LocalTime.now(), LocalTime.now())

        request = ConvertTripPatternRequest(
            id = "",
            legs = listOf(
                Leg(
                    id = "",
                    travelDate = travelDate,
                    fromStopPlaceId = fromStopPlaceId,
                    toStopPlaceId = toStopPlaceId,
                    serviceJourneyId = "SE:050:ServiceJourney:121120000338664260"
                )
            )
        )
    }

    @Test
    fun mapDepartureTime() {
        serviceJourney.addPassingTime(fromStopPlaceId, "00:00", "13:00")

        val convertTripPatternResponse = osdmConverterController.convertTripPattern(request)

        assertEquals(
            "${travelDate}T13:00+02:00[Europe/Stockholm]",
            convertTripPatternResponse.tripSpecification.legs[0].timedLeg!!.start.serviceDeparture.timetabledTime.toString()
        )
    }

    @Test
    fun mapArrivalTime() {
        serviceJourney.addPassingTime(toStopPlaceId, "16:00", "00:00")

        val convertTripPatternResponse = osdmConverterController.convertTripPattern(request)

        assertEquals(
            "2024-05-01T16:00+02:00[Europe/Stockholm]",
            convertTripPatternResponse.tripSpecification.legs[0].timedLeg!!.end.serviceArrival.timetabledTime.toString()
        )
    }

    @Test
    fun mapDepartureStop() {
        stopsRepository.addStop(fromStopPlaceId, 6000001)

        val convertTripPatternResponse = osdmConverterController.convertTripPattern(request)

        assertEquals(
            "urn:x_swe:stn:6000001",
            convertTripPatternResponse.tripSpecification.legs[0].timedLeg!!.start.stopPlaceRef.stopPlaceRef
        )
    }

    @Test
    fun mapArrivalStop() {
        stopsRepository.addStop(toStopPlaceId, 6000002)

        val convertTripPatternResponse = osdmConverterController.convertTripPattern(request)

        assertEquals(
            "urn:x_swe:stn:6000002",
            convertTripPatternResponse.tripSpecification.legs[0].timedLeg!!.end.stopPlaceRef.stopPlaceRef
        )
    }

    @Test
    fun mapProductCategory() {
        serviceJourney.setValue("productCode", "RE")

        val convertTripPatternResponse = osdmConverterController.convertTripPattern(request)
        assertEquals(
            "urn:x_swe:sbc:RE",
            convertTripPatternResponse.tripSpecification.legs[0].timedLeg!!.service.productCategory!!.productCategoryRef
        )
    }

    @Test
    fun mapSalesAuthorityNumber() {
        serviceJourney.line.setValue("salesAuthorityNumber", "456")

        val convertTripPatternResponse = osdmConverterController.convertTripPattern(request)
        assertEquals(
            "urn:x_swe:carrier:456",
            convertTripPatternResponse.tripSpecification.legs[0].timedLeg!!.service.carriers[0].ref
        )
    }

    @Test
    fun mapVehicleNumbersFromTrainNumbers() {
        serviceJourney.addTrainNumber("620")
        val convertTripPatternResponse = osdmConverterController.convertTripPattern(request)
        assertEquals(
            "620",
            convertTripPatternResponse.tripSpecification.legs[0].timedLeg!!.service.vehicleNumbers[0]
        )
    }

    @Test
    fun mapVehicleNumbersFromLineNumber() {
        serviceJourney.trainNumbers.clear()
        serviceJourney.line.publicCode = "620"

        val convertTripPatternResponse = osdmConverterController.convertTripPattern(request)

        assertEquals(
            "620",
            convertTripPatternResponse.tripSpecification.legs[0].timedLeg!!.service.vehicleNumbers[0]
        )
    }

    @Test
    fun mapTransportMode() {
        assertModeMapping("bus", "BUS")
        assertModeMapping("water", "SHIP")
        assertModeMapping("rail", "TRAIN")
        assertModeMapping("tram", "TRAM")
        assertModeMapping("taxi", "SHARED_TAXI")
        assertModeMapping("metro", "UNDERGROUND")
    }

    private fun assertModeMapping(netexMode: String, osdmMode: String) {
        serviceJourney.transportMode = netexMode

        val convertTripPatternResponse = osdmConverterController.convertTripPattern(request)
        assertEquals(
            osdmMode,
            convertTripPatternResponse.tripSpecification.legs[0].timedLeg!!.service.mode?.ptMode
        )
    }
}

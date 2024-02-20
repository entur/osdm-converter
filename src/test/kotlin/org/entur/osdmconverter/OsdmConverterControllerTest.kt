package org.entur.osdmconverter

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

    private val stopId = "SE:050:StopPlace:9021050000003000_2"
    private val stopId2 = "SE:050:StopPlace:9021050025315000_2"
    private lateinit var serviceJourney: ServiceJourney
    private lateinit var request: ConvertTripPatternRequest

    @BeforeEach
    fun setUp() {
        stopsRepository = StopsRepository()
        serviceJourneyRepository = ServiceJourneyRepository()
        osdmConverterController = OsdmConverterController(stopsRepository, serviceJourneyRepository)

        stopsRepository.addStop(stopId, 6000001)
        stopsRepository.addStop(stopId2, 6000002)

        serviceJourney = serviceJourneyRepository.addServiceJourney("SE:050:ServiceJourney:121120000338664260")
        serviceJourney.addPassingTime(LocalTime.now(), LocalTime.now(), stopId)
        serviceJourney.addPassingTime(LocalTime.now(), LocalTime.now(), stopId2)

        request = ConvertTripPatternRequest(
            id = "",
            legs = listOf(
                ConvertTripPatternRequest.Leg(
                    id = "",
                    travelDate = LocalDate.now(),
                    fromStopPlaceId = stopId,
                    toStopPlaceId = stopId2,
                    serviceJourneyId = "SE:050:ServiceJourney:121120000338664260"
                )
            )
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

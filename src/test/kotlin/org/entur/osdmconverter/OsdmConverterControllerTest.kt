package org.entur.osdmconverter

import org.entur.osdmconverter.client.journeyplanner.ServiceJourney
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime

class OsdmConverterControllerTest {
    lateinit var stopsRepository: StopsRepository
    lateinit var osdmConverterController: OsdmConverterController
    lateinit var serviceJourneyRepository: ServiceJourneyRepository

    @BeforeEach
    fun setUp() {
        stopsRepository = StopsRepository()
        serviceJourneyRepository = ServiceJourneyRepository()
        osdmConverterController = OsdmConverterController(stopsRepository, serviceJourneyRepository)
    }

    @Test
    fun mapProductCategory() {

        val stopId = "SE:050:StopPlace:9021050000003000_2"
        val stopId2 = "SE:050:StopPlace:9021050025315000_2"
        val passingTimes = listOf(
            ServiceJourney.PassingTime(
                arrival = ServiceJourney.Time(LocalTime.now(), 0),
                departure = ServiceJourney.Time(LocalTime.now(), 0),
                quay = ServiceJourney.Quay(ServiceJourney.IdName(stopId, "test"))
            ),
            ServiceJourney.PassingTime(
                arrival = ServiceJourney.Time(LocalTime.now(), 0),
                departure = ServiceJourney.Time(LocalTime.now(), 0),
                quay = ServiceJourney.Quay(ServiceJourney.IdName(stopId2, "test"))
            )
        )

        stopsRepository.addStop(stopId, 6000001)
        stopsRepository.addStop(stopId2, 6000002)
        serviceJourneyRepository.addServiceJourney("SE:050:ServiceJourney:121120000338664260", "RE", passingTimes)

        val request = ConvertTripPatternRequest(
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
        val convertTripPatternResponse = osdmConverterController.convertTripPattern(request)
        assertEquals(
            "urn:x_swe:sbc:RE",
            convertTripPatternResponse.tripSpecification.legs.get(0).timedLeg!!.service.productCategory!!.productCategoryRef
        )
        assertNotNull(convertTripPatternResponse)
    }
}
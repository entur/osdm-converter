package org.entur.osdmconverter

import io.osdm.*
import org.entur.osdmconverter.ror.JourneyPlannerApi
import org.entur.osdmconverter.samtrafiken.SamtrafikenApi
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.CompletableFuture


@Service
class SamtrafikenConverter(
    private val journeyPlannerApi: JourneyPlannerApi,
    private val samtrafikenApi: SamtrafikenApi)  {

    private val serviceJourneyQuery = """
            query (${"$"}id: String!) {
              serviceJourney(
                id: ${"$"}id
              ) {
                id
                transportMode
                transportSubmode
                line {
                  id
                  publicCode
                  authority {
                    id
                    name
                  }
                  operator {
                    id
                    name
                  }
                }
                passingTimes {
                  quay {
                    stopPlace {
                      id
                      name
                    }
                  }
                  departure {
                    time
                    dayOffset
                  }
                  arrival {
                    time
                    dayOffset
                  }
                }
              }
            }
        """.replace("\n", " ")

    /**
     * Temporary solution for mapping SearchTripPatternRequest to OfferCollectionRequest, by using journey-planner
     * and Samtrafiken api with TripSearchCriteria to find missing info like vehicle numbers
     *
     * Details:
     * https://app.mural.co/t/entur7578/m/entur7578/1707219234049/1ace25937be3aaf1eebc8cd4ef3774a78e2d1b95?sender=u68d163d15cdd580845376154
     */
     fun convertTripPattern(convertTripPatternRequest: ConvertTripPatternRequest): ConvertTripPatternResponse {
        val tripParamsFutures = convertTripPatternRequest.legs.map { CompletableFuture.supplyAsync { tripParamsFromJourneyPlanner(it) } }
        val tripParams = tripParamsFutures.map { it.get() }
        val offerCollectionRequest = offerRequestWithTripSearch(tripParams)
        val offerCollectionResponse = samtrafikenApi.getOffers(offerCollectionRequest)
        val tripLegSpecifications: List<TripLegSpecification> = tripParams.map { tp ->
            val trip = offerCollectionResponse.trips!!.find { t ->  tp.matchesTrip(t) }!!
            trip.legs.map { leg ->
                TripLegSpecification(
                    externalRef = tp.leg.serviceJourneyId,
                    timedLeg = TimedLegSpecification(
                        start = BoardSpecification(
                            stopPlaceRef = leg.timedLeg!!.start.stopPlaceRef,
                            serviceDeparture = leg.timedLeg.start.serviceDeparture
                        ),
                        end = AlightSpecification(
                            stopPlaceRef = leg.timedLeg.end.stopPlaceRef,
                            serviceArrival = leg.timedLeg.end.serviceArrival,
                        ),
                        service = leg.timedLeg.service
                    ),
                )
            }
        }.flatten()

        return ConvertTripPatternResponse(
            tripSpecification = TripSpecification(
                legs = tripLegSpecifications,
                externalRef = convertTripPatternRequest.id,
                isPartOfInternationalTrip = false
            )
        )
    }

            // Call journey-planner to find the arrival and departure times
    private fun tripParamsFromJourneyPlanner(leg: Leg): TripParam {
        val graphQlRequest = JourneyPlannerApi.GraphQlRequest(serviceJourneyQuery, mapOf("id" to leg.serviceJourneyId))
        val res = journeyPlannerApi.getServiceJourney(graphQlRequest)

        val fromPlace = res.data.serviceJourney.getPassingTime(leg.fromStopPlaceId)!!
        val departureTime = zonedDateTime(leg, fromPlace.departure.time)

        val toPlace = res.data.serviceJourney.getPassingTime(leg.toStopPlaceId)!!
        val arrivalTime = zonedDateTime(leg, toPlace.arrival.time)

        return TripParam(departure = departureTime, arrival = arrivalTime, leg = leg)
    }

    private data class TripParam(
        val departure: ZonedDateTime,
        val arrival: ZonedDateTime,
        val leg: Leg
    ) {
        fun matchesTrip(it: Trip): Boolean {
            return (it.origin as StopPlaceRef).stopPlaceRef == mapStopPlace(leg.fromStopPlaceId)
                    && (it.destination as StopPlaceRef).stopPlaceRef == mapStopPlace(leg.toStopPlaceId)
                    && it.startTime.toInstant() == departure.toInstant()
                    && it.endTime.toInstant() == arrival.toInstant()
        }
    }


    private fun zonedDateTime(
        leg: Leg,
        localTime: LocalTime,
    ): ZonedDateTime {
        val zonedDateTime = LocalDateTime.of(leg.travelDate, localTime).atZone(ZoneId.of("Europe/Oslo"))
        return ZonedDateTime.of(leg.travelDate, localTime, zonedDateTime.offset)
    }


    private fun offerRequestWithTripSearch(
        tripParams: List<TripParam>
    ): OfferCollectionRequest {
        val departure = tripParams.first().departure
        return OfferCollectionRequest(
            anonymousPassengerSpecifications = listOf(
                AnonymousPassengerSpecification(
                    externalRef = "123456789",
                    type = "PERSON"
                )
            ),
            tripSearchCriteria = TripSearchCriteria(
                departureTime = departure.toString(),
                origin = StopPlaceRef(
                    stopPlaceRef = mapStopPlace(tripParams.first().leg.fromStopPlaceId),
                    objectType = "StopPlaceRef"
                ), destination = StopPlaceRef(
                    stopPlaceRef = mapStopPlace(tripParams.last().leg.toStopPlaceId),
                    objectType = "StopPlaceRef"
                )
            ),
            offerSearchCriteria = OfferSearchCriteria(
                currency = "SEK",
            ),
            embed = listOf(
                OfferCollectionResponseContent.ALL
            )
        )
    }

    private companion object {
        fun mapStopPlace(stopPlaceId: String): String {
            return when (stopPlaceId) {
                // Oslo
                "NSR:StopPlace:337" -> "urn:x_swe:stn:760000100"
                // Gøteborg
                "NSR:StopPlace:374" -> "urn:x_swe:stn:740000002"
                // København
                "NSR:StopPlace:63172" -> "urn:x_swe:stn:860000626"
                else -> stopPlaceId
            }
        }
    }
}

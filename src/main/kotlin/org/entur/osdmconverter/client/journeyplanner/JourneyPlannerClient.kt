package org.entur.osdmconverter.client.journeyplanner

import org.entur.osdmconverter.client.GraphQlRequest
import org.entur.osdmconverter.client.clientFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class JourneyPlannerClient(@Value("\${clients.journeyPlanner.url}") private val url: String) {

    private val journeyPlannerApi = clientFactory(JourneyPlannerApi::class.java, url)

    private val serviceJourneyQuery =
        """
            query journey {
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

    fun getServiceJourney(
        id: String
    ): ServiceJourney? {
        val graphQlRequest = GraphQlRequest(serviceJourneyQuery, variables = mapOf("id" to id))

        println(graphQlRequest.requestBody)

        return journeyPlannerApi
            .getServiceJourney(graphQlRequest.requestBody)
            .data
            .serviceJourney
    }
}

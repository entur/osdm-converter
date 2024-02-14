package org.entur.osdmconverter.client.journeyplanner

import feign.Body
import feign.Headers
import feign.Param
import feign.RequestLine

@Headers("Content-Type: application/json")
interface JourneyPlannerApi {

  @RequestLine(
      "POST",
  )
  @Body("{body}")
  fun getServiceJourney(
      @Param("body") body: String,
  ): GetServiceJourneyResponse
}

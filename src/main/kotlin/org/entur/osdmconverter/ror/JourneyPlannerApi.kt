package org.entur.osdmconverter.ror

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(value = "journey-planner")
interface JourneyPlannerApi {

    @PostMapping(produces = ["application/json"], consumes = ["application/json"])
    fun getServiceJourney(
        @RequestBody body: GraphQlRequest,
    ): GetServiceJourneyResponse

    data class GraphQlRequest(val query: String, val variables: Map<String, Any> = emptyMap())
}

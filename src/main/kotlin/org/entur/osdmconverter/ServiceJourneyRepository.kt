package org.entur.osdmconverter

import com.google.common.cache.CacheBuilder.newBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import org.entur.osdmconverter.client.journeyplanner.JourneyPlannerClient
import org.entur.osdmconverter.client.journeyplanner.ServiceJourney
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class ServiceJourneyRepository(private val journeyPlannerClient: JourneyPlannerClient) {
    var deviceCache: LoadingCache<String, ServiceJourney> =
        newBuilder()
            .refreshAfterWrite(300, TimeUnit.SECONDS)
            .build(CacheLoader.from(journeyPlannerClient::getServiceJourney))

    fun getServiceJourney(serviceJourneyId: String): ServiceJourney {
        return deviceCache.get(serviceJourneyId)
    }
}

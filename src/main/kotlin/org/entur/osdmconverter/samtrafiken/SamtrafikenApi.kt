package org.entur.osdmconverter.samtrafiken

import io.osdm.*
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(value = "samtrafiken", url = "https://api.ds.int.samtrafiken.turnit.tech/sales", configuration = [SamtrafikenConfig::class])
interface SamtrafikenApi {

    @PostMapping("/offers", produces = ["application/json; version=3.0.2; charset=utf-8"])
    fun getOffers(
        @RequestBody offerCollectionRequest: OfferCollectionRequest
    ): OfferCollectionResponse

    @PostMapping("/bookings", produces = ["application/json; version=3.0.2; charset=utf-8"])
    fun createBooking(
        @RequestBody body: BookingRequest
    ): BookingResponse
    @GetMapping("/bookings/{bookingId}", produces = ["application/json; version=3.0.2; charset=utf-8"])
    fun getBooking(
        @PathVariable("bookingId") bookingId: String,
    ): BookingResponse

    @PostMapping("/bookings/{bookingId}/fulfillments", produces = ["application/json; version=3.0.2; charset=utf-8"])
    fun triggerFulfillment(
        @PathVariable("bookingId") bookingId: String,
    ): String

    @GetMapping("/fulfillments/{fulfillmentsId}", produces = ["application/json; version=3.0.2; charset=utf-8"])
    fun getFulfillment(
        @PathVariable("fulfillmentsId") fulfillmentsId: String,
    ): FulfillmentResponse

    @GetMapping("/places", produces = ["application/json; version=3.0.2; charset=utf-8"])
    fun getPlaces(): PlaceResponse

}

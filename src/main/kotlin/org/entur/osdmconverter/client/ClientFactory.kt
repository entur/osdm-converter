package org.entur.osdmconverter.client

import com.google.gson.GsonBuilder
import feign.Feign
import feign.Logger
import feign.gson.GsonDecoder
import feign.gson.GsonEncoder
import feign.okhttp.OkHttpClient
import feign.slf4j.Slf4jLogger
import org.entur.osdmconverter.client.adapters.*
import java.time.*

fun <T> clientFactory(clientAPI: Class<T>, url: String): T {

    val gson =
        GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            .registerTypeAdapter(LocalTime::class.java, LocalTimeAdapter())
            .registerTypeAdapter(LocalDateTime::class.java, LocaleDateTimeAdapter())
            .registerTypeAdapter(OffsetDateTime::class.java, OffsetDateTimeAdapter())
            .registerTypeAdapter(ZonedDateTime::class.java, ZonedDateTimeAdapter())
            .create()

    return Feign.builder()
        .client(OkHttpClient())
        .encoder(GsonEncoder(gson))
        .decoder(GsonDecoder(gson))
        .logger(Slf4jLogger())
        .requestInterceptor(CorrelationIdInterceptor())
        .requestInterceptor(EtClientNameInterceptor())
        .logLevel(Logger.Level.FULL)
        .target(clientAPI, url)
}

package org.entur.osdmconverter.samtrafiken

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import feign.RequestInterceptor
import feign.RequestTemplate
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.TimeUnit


class SamtrafikenConfig(
    @Value("\${samtrafiken.clientId}") val clientId: String,
    @Value("\${samtrafiken.secret}") val secret: String,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    private val cache: Cache<String, TokenResponse> = Caffeine.newBuilder()
        .expireAfterWrite(55, TimeUnit.MINUTES)
        .build()

    @Bean
    fun osdmObjectMapper(): ObjectMapper {
        val objectMapper = ObjectMapper()
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        objectMapper.registerModule(JavaTimeModule())

        return objectMapper
    }

    @Bean
    fun osdmRequestInterceptor(): RequestInterceptor {
        return RequestInterceptor { requestTemplate: RequestTemplate ->
            val token = cache.get("token" ) { getToken() }
            val auth = "Bearer ${token.accessToken}"
            requestTemplate.header(
                "Authorization",
                auth
            ).header("Requestor", "ewogICJzYWxlc0FwcGxpY2F0aW9uIjogIkVOVFVSIiwKICAic2FsZXNVbml0Q29kZSI6ICJTVDMwMDAwMSIKfQo=")
        }
    }

    private fun getToken(): TokenResponse {
        log.info("fetch Samtrafiken token")
        val formData: MutableMap<String, String> = HashMap()
        formData["grant_type"] = "client_credentials"

        val encoded = Base64.getEncoder().encodeToString(("$clientId:$secret").toByteArray())
        // We use HttpClient since we did not get Feign to work with: application/x-www-form-urlencoded
        val client = HttpClient.newHttpClient();
        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://identity.ds.int.samtrafiken.turnit.tech/connect/token"))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("Authorization", "Basic $encoded")
            .POST(HttpRequest.BodyPublishers.ofString(getFormDataAsString(formData)))
            .build()

        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        val mapper = ObjectMapper()
        val token = mapper.readValue(response.body(), TokenResponse::class.java)

        return token!!
    }

    private fun getFormDataAsString(formData: Map<String, String>): String {
        val formBodyBuilder = StringBuilder()
        for ((key, value) in formData) {
            if (formBodyBuilder.isNotEmpty()) {
                formBodyBuilder.append("&")
            }
            formBodyBuilder.append(URLEncoder.encode(key, StandardCharsets.UTF_8))
            formBodyBuilder.append("=")
            formBodyBuilder.append(URLEncoder.encode(value, StandardCharsets.UTF_8))
        }
        return formBodyBuilder.toString()
    }

}

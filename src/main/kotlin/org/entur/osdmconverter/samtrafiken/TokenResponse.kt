package org.entur.osdmconverter.samtrafiken

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class TokenResponse(@JsonProperty("access_token") val accessToken: String)

package org.entur.osdmconverter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication
@EnableFeignClients
class OsdmConverterApplication

fun main(args: Array<String>) {
    runApplication<OsdmConverterApplication>(*args)
}

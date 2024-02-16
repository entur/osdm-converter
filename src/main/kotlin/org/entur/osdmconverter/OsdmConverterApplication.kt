package org.entur.osdmconverter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@SpringBootApplication
@EnableFeignClients
@Configuration
class OsdmConverterApplication {


    @Bean
    fun stopsRepository(): StopsRepository {
        val stopsRepository = StopsRepository()
        stopsRepository.readFile(this::javaClass.javaClass.classLoader.getResourceAsStream("_minimal_stops.xml"))
        return stopsRepository
    }
}
fun main(args: Array<String>) {
    runApplication<OsdmConverterApplication>(*args)
}

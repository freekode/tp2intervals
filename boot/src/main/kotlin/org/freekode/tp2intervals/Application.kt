package org.freekode.tp2intervals

import org.freekode.tp2intervals.infrastructure.configuration.DefaultConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableFeignClients
@EnableCaching
@EnableScheduling
@EnableConfigurationProperties(DefaultConfiguration::class)
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

package org.freekode.tp2intervals

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableFeignClients
@EnableCaching
@EnableScheduling
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

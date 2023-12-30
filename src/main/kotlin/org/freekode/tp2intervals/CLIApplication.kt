package org.freekode.tp2intervals

import org.freekode.tp2intervals.infrastructure.intervalsicu.IntervalsProperties
import org.freekode.tp2intervals.infrastructure.thirdparty.ThirdPartyProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication
@EnableFeignClients
@EnableConfigurationProperties(
    IntervalsProperties::class, ThirdPartyProperties::class
)
class CLIApplication : CommandLineRunner {
    override fun run(vararg args: String) {
        TODO("Not yet implemented")
    }
}

val log: Logger = LoggerFactory.getLogger(CLIApplication::class.java)

fun main(args: Array<String>) {
    log.info("Starting the app")
    runApplication<CLIApplication>(*args)
    log.info("The app finished")
}

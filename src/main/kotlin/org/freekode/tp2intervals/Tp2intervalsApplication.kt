package org.freekode.tp2intervals

import org.freekode.tp2intervals.infrastructure.intervalsicu.IntervalsProperties
import org.freekode.tp2intervals.infrastructure.thirdparty.ThirdPartyProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@EnableFeignClients
@SpringBootApplication
@EnableConfigurationProperties(
	IntervalsProperties::class, ThirdPartyProperties::class
)
class Tp2intervalsApplication

fun main(args: Array<String>) {
	runApplication<Tp2intervalsApplication>(*args)
}

package org.freekode.tp2intervals

import org.freekode.tp2intervals.infrastructure.intervalsicu.IntervalsProperties
import org.freekode.tp2intervals.infrastructure.trainingpeaks.TrainingPeaksProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication
@EnableFeignClients
@EnableConfigurationProperties(
    IntervalsProperties::class, TrainingPeaksProperties::class
)
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

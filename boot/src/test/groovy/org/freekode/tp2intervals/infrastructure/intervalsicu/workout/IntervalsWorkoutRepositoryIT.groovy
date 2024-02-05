package org.freekode.tp2intervals.infrastructure.intervalsicu.workout

import com.fasterxml.jackson.databind.ObjectMapper
import org.freekode.tp2intervals.config.ISpringConfiguration
import org.freekode.tp2intervals.config.MockIntervalsApiClient
import org.freekode.tp2intervals.domain.config.AppConfigurationRepository
import org.freekode.tp2intervals.domain.config.UpdateConfigurationRequest
import org.freekode.tp2intervals.infrastructure.intervalsicu.IntervalsApiClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.core.io.Resource

import java.nio.charset.Charset
import java.time.LocalDate

class IntervalsWorkoutRepositoryIT extends ISpringConfiguration {
    @Autowired
    IntervalsWorkoutRepository intervalsWorkoutRepository

    @Autowired
    AppConfigurationRepository appConfigurationRepository

    def setup() {
        appConfigurationRepository.updateConfig(new UpdateConfigurationRequest(
                Map.of(
                        "intervals.api-key", "my-key",
                        "intervals.athlete-id", "my-athlete"
                )
        ))
    }

    def "should parse and map planned workouts"() {
        when:
        def workouts = intervalsWorkoutRepository.getPlannedWorkouts(LocalDate.now(), LocalDate.now())

        then:
        workouts != null
    }

    @TestConfiguration
    class IntervalsTestConfiguration {
        @Bean
        @Primary
        IntervalsApiClient intervalsApiClient(
                ObjectMapper objectMapper,
                @Value("classpath:intervals-events-response-note.json") Resource eventsResponse) {
            new MockIntervalsApiClient(objectMapper, eventsResponse.getContentAsString(Charset.defaultCharset()))
        }
    }
}

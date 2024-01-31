package org.freekode.tp2intervals.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.freekode.tp2intervals.infrastructure.intervalsicu.IntervalsApiClient
import org.freekode.tp2intervals.infrastructure.trainerroad.TrainerRoadApiClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.core.io.Resource

import java.nio.charset.Charset

@TestConfiguration
class ITestConfiguration {
    @Bean
    @Primary
    IntervalsApiClient intervalsApiClient(
            ObjectMapper objectMapper,
            @Value("classpath:intervals-events-response.json") Resource eventsResponse) {
        new MockIntervalsApiClient(objectMapper, eventsResponse.getContentAsString(Charset.defaultCharset()))
    }

    @Bean
    @Primary
    TrainerRoadApiClient trainerRoadApiClient(
            ObjectMapper objectMapper,
            @Value("classpath:trainer-road-activities-response.json") Resource activitiesResponse,
            @Value("classpath:workout.zwo") Resource workoutFile) {
        new MockTrainerRoadApiClient(
                objectMapper,
                activitiesResponse.getContentAsString(Charset.defaultCharset()),
                workoutFile
        )
    }


}

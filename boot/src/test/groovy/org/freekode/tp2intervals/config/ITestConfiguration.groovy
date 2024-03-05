package org.freekode.tp2intervals.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.IntervalsApiClient
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsAthleteApiClient
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.TrainerRoadApiClient
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.token.TrainingPeaksTokenApiClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.core.io.Resource

import java.nio.charset.Charset

@TestConfiguration
class ITestConfiguration {
    @ConditionalOnProperty(name = "dev.mock", havingValue = "true")
    @Bean
    @Primary
    IntervalsApiClient intervalsApiClient(
            ObjectMapper objectMapper,
            @Value("classpath:intervals-events-response.json") Resource eventsResponse) {
        new MockIntervalsApiClient(objectMapper, eventsResponse.getContentAsString(Charset.defaultCharset()))
    }

    @ConditionalOnProperty(name = "dev.mock", havingValue = "true")
    @Bean
    @Primary
    IntervalsAthleteApiClient intervalsAthleteApiClient() {
        new MockIntervalsAthleteApiClient()
    }


    @ConditionalOnProperty(name = "dev.mock", havingValue = "true")
    @Bean
    @Primary
    TrainingPeaksTokenApiClient trainingPeaksTokenApiClient() {
        new MockTrainingPeaksTokenApiClient()
    }

    @ConditionalOnProperty(name = "dev.mock", havingValue = "true")
    @Bean
    @Primary
    TrainerRoadApiClient trainerRoadApiClient(
            ObjectMapper objectMapper,
            @Value("classpath:trainer-road-activities-response.json") Resource activitiesResponse,
            @Value("classpath:trainer-road-workout-details-abney.json") Resource workoutDetailsResponseAbney,
            @Value("classpath:trainer-road-workout-details-obelisk.json") Resource workoutDetailsResponseObelisk,
            @Value("classpath:workout.zwo") Resource workoutFile) {
        new MockTrainerRoadApiClient(
                objectMapper,
                workoutFile,
                activitiesResponse.getContentAsString(Charset.defaultCharset()),
                workoutDetailsResponseAbney.getContentAsString(Charset.defaultCharset()),
                workoutDetailsResponseObelisk.getContentAsString(Charset.defaultCharset()),
        )
    }


}

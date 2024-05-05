package org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout

import feign.codec.DecodeException
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.TrainerRoadApiClient
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository

@CacheConfig(cacheNames = ["trWorkoutCache"])
@Repository
class TRInternalWorkoutRepository(
    private val trainerRoadApiClient: TrainerRoadApiClient,
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    fun getWorkout(trWorkoutId: String): Workout {
        val trWorkoutConverter = TRWorkoutConverter()
        try {
            return trainerRoadApiClient.getWorkout(trWorkoutId)
                .let { trWorkoutConverter.toWorkout(it) }
        } catch (e: DecodeException) {
            log.warn("Decode exception while fetching workout", e)
            throw e
        }

    }
}

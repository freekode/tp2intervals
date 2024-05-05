package org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout

import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.TrainerRoadApiClient
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository

@CacheConfig(cacheNames = ["trWorkoutCache"])
@Repository
class TRInternalWorkoutRepository(
    private val trainerRoadApiClient: TrainerRoadApiClient,
) {
    @Cacheable
    fun getWorkout(trWorkoutId: String): Workout {
        val trWorkoutConverter = TRWorkoutConverter()
        return trainerRoadApiClient.getWorkout(trWorkoutId)
            .let { trWorkoutConverter.toWorkout(it) }
    }
}

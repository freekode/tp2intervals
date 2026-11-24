package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.CreateTPWorkoutRequestDTO
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
    value = "TrainingPeaksReconciliationApiClient",
    url = "\${app.training-peaks.api-url}",
    primary = false,
    configuration = [TrainingPeaksApiClientConfig::class],
)
interface TrainingPeaksReconciliationApiClient {
    @GetMapping("/fitness/v6/athletes/{userId}/workouts/{startDate}/{endDate}")
    fun getWorkouts(
        @PathVariable userId: String,
        @PathVariable startDate: String,
        @PathVariable endDate: String,
    ): List<ObjectNode>

    @GetMapping("/fitness/v6/athletes/{userId}/workouts/{workoutId}")
    fun getWorkout(@PathVariable userId: String, @PathVariable workoutId: String): ObjectNode

    @PostMapping("/fitness/v6/athletes/{userId}/workouts")
    fun createWorkout(@PathVariable userId: String, @RequestBody request: CreateTPWorkoutRequestDTO): JsonNode

    @PutMapping("/fitness/v6/athletes/{userId}/workouts/{workoutId}")
    fun updateWorkout(
        @PathVariable userId: String,
        @PathVariable workoutId: String,
        @RequestBody request: ObjectNode,
    ): JsonNode

    @DeleteMapping("/fitness/v6/athletes/{userId}/workouts/{workoutId}")
    fun deleteWorkout(@PathVariable userId: String, @PathVariable workoutId: String): Boolean?
}

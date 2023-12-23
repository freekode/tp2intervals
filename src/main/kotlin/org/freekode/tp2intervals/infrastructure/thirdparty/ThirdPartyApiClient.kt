package org.freekode.tp2intervals.infrastructure.thirdparty

import org.freekode.tp2intervals.infrastructure.thirdparty.user.ThirdPartyUserDTO
import org.freekode.tp2intervals.infrastructure.thirdparty.workout.CreateThirdPartyWorkoutDTO
import org.freekode.tp2intervals.infrastructure.thirdparty.workout.ThirdPartyNoteDTO
import org.freekode.tp2intervals.infrastructure.thirdparty.workout.ThirdPartyWorkoutDTO
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
    value = "ThirdPartyApiClient",
    url = "\${third-party.api-url}",
    dismiss404 = true,
    primary = false,
    configuration = [ThirdPartyApiClientConfig::class]
)
interface ThirdPartyApiClient {
    @GetMapping("/users/v3/user")
    fun getUser(): ThirdPartyUserDTO

    @GetMapping("/fitness/v6/athletes/{userId}/workouts/{startDate}/{endDate}")
    fun getWorkouts(
        @PathVariable("userId") userId: String,
        @PathVariable("startDate") startDate: String,
        @PathVariable("endDate") endDate: String
    ): List<ThirdPartyWorkoutDTO>

    @GetMapping("/fitness/v1/athletes/{userId}/calendarNote/{startDate}/{endDate}")
    fun getNotes(
        @PathVariable("userId") userId: String,
        @PathVariable("startDate") startDate: String,
        @PathVariable("endDate") endDate: String
    ): List<ThirdPartyNoteDTO>

    @GetMapping("/fitness/v6/athletes/{userId}/workouts/{workoutId}/fordevice/zwo")
    fun downloadWorkoutZwo(
        @PathVariable("userId") userId: String,
        @PathVariable("workoutId") workoutId: String,
    ): String

    @PostMapping("/fitness/v6/athletes/{userId}/workouts")
    fun createAndPlanWorkout(
        @PathVariable("userId") userId: String,
        @RequestBody createThirdPartyWorkoutDTO: CreateThirdPartyWorkoutDTO
    )
}

package org.freekode.tp2intervals.infrastructure.intervalsicu.workout


import org.freekode.tp2intervals.config.ISpringConfiguration
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.config.AppConfigurationRepository
import org.freekode.tp2intervals.domain.config.UpdateConfigurationRequest
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.structure.WorkoutSingleStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure
import org.springframework.beans.factory.annotation.Autowired

import java.time.Duration
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

    def "should parse hr workout"() {
        when:
        def workouts = intervalsWorkoutRepository.getPlannedWorkouts(LocalDate.now(), LocalDate.now())

        then:
        workouts.size() == 5

        def hrWorkout = findWorkoutWithName("hr test", workouts)
        hrWorkout.type == TrainingType.BIKE
        hrWorkout.structure.target == WorkoutStructure.TargetUnit.LTHR_PERCENTAGE
        hrWorkout.structure.steps.size() == 5
        (hrWorkout.structure.steps[0] as WorkoutSingleStep).duration == Duration.ofMinutes(10)
        (hrWorkout.structure.steps[0] as WorkoutSingleStep).target.start == 50
        (hrWorkout.structure.steps[0] as WorkoutSingleStep).target.end == 70
        (hrWorkout.structure.steps[1] as WorkoutSingleStep).target.start == 78
        (hrWorkout.structure.steps[1] as WorkoutSingleStep).target.end == 82
        (hrWorkout.structure.steps[2] as WorkoutSingleStep).target.start == 45
        (hrWorkout.structure.steps[2] as WorkoutSingleStep).target.end == 60
        (hrWorkout.structure.steps[2] as WorkoutSingleStep).ramp
    }

    def findWorkoutWithName(name, List<Workout> workouts) {
        workouts.stream().filter { it.getTitle() == name }.findFirst().orElseThrow()
    }
}

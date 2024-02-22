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
        // 10m 50-70% LTHR
        (hrWorkout.structure.steps[0] as WorkoutSingleStep).duration == Duration.ofMinutes(10)
        (hrWorkout.structure.steps[0] as WorkoutSingleStep).target.start == 50
        (hrWorkout.structure.steps[0] as WorkoutSingleStep).target.end == 70
        // 10m 80% LTHR
        (hrWorkout.structure.steps[1] as WorkoutSingleStep).target.start == 78
        (hrWorkout.structure.steps[1] as WorkoutSingleStep).target.end == 82
        // 10m ramp 45-60% LTHR
        (hrWorkout.structure.steps[2] as WorkoutSingleStep).target.start == 45
        (hrWorkout.structure.steps[2] as WorkoutSingleStep).target.end == 60
        (hrWorkout.structure.steps[2] as WorkoutSingleStep).ramp
        // 10m Z2 HR
        (hrWorkout.structure.steps[3] as WorkoutSingleStep).target.start == 68
        (hrWorkout.structure.steps[3] as WorkoutSingleStep).target.end == 82
        // 10m Z3-Z4 HR
        (hrWorkout.structure.steps[4] as WorkoutSingleStep).target.start == 83
        (hrWorkout.structure.steps[4] as WorkoutSingleStep).target.end == 105
    }

    def "should parse power workout"() {
        when:
        def workouts = intervalsWorkoutRepository.getPlannedWorkouts(LocalDate.now(), LocalDate.now())

        then:
        workouts.size() == 5

        def powerWorkout = findWorkoutWithName("power test", workouts)
        powerWorkout.type == TrainingType.BIKE
        powerWorkout.structure.target == WorkoutStructure.TargetUnit.FTP_PERCENTAGE
        powerWorkout.structure.steps.size() == 5
        // 10m 10-30%
        (powerWorkout.structure.steps[0] as WorkoutSingleStep).duration == Duration.ofMinutes(10)
        (powerWorkout.structure.steps[0] as WorkoutSingleStep).target.start == 10
        (powerWorkout.structure.steps[0] as WorkoutSingleStep).target.end == 30
        // 10m 40% 30-90rpm
        (powerWorkout.structure.steps[1] as WorkoutSingleStep).target.start == 39
        (powerWorkout.structure.steps[1] as WorkoutSingleStep).target.end == 41
        (powerWorkout.structure.steps[1] as WorkoutSingleStep).cadence.start == 30
        (powerWorkout.structure.steps[1] as WorkoutSingleStep).cadence.end == 90
        // 10m ramp 10-60%
        (powerWorkout.structure.steps[2] as WorkoutSingleStep).target.start == 10
        (powerWorkout.structure.steps[2] as WorkoutSingleStep).target.end == 60
        (powerWorkout.structure.steps[2] as WorkoutSingleStep).ramp
        // 10m Z2 85rpm
        (powerWorkout.structure.steps[3] as WorkoutSingleStep).target.start == 56
        (powerWorkout.structure.steps[3] as WorkoutSingleStep).target.end == 75
        (powerWorkout.structure.steps[3] as WorkoutSingleStep).cadence.start == 85
        (powerWorkout.structure.steps[3] as WorkoutSingleStep).cadence.end == 85
        // 10m Z3-Z4
        (powerWorkout.structure.steps[4] as WorkoutSingleStep).target.start == 76
        (powerWorkout.structure.steps[4] as WorkoutSingleStep).target.end == 105
    }


    def findWorkoutWithName(name, List<Workout> workouts) {
        workouts.stream().filter { it.getTitle() == name }.findFirst().orElseThrow()
    }
}

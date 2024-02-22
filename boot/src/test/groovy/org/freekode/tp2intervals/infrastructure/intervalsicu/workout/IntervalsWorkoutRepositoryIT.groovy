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

        def workout = findWorkoutWithName("hr test", workouts)
        workout.type == TrainingType.BIKE
        workout.structure.target == WorkoutStructure.TargetUnit.LTHR_PERCENTAGE
        workout.structure.steps.size() == 5
        // 10m 50-70% LTHR
        (workout.structure.steps[0] as WorkoutSingleStep).duration == Duration.ofMinutes(10)
        (workout.structure.steps[0] as WorkoutSingleStep).target.start == 50
        (workout.structure.steps[0] as WorkoutSingleStep).target.end == 70
        // 10m 80% LTHR
        (workout.structure.steps[1] as WorkoutSingleStep).target.start == 78
        (workout.structure.steps[1] as WorkoutSingleStep).target.end == 82
        // 10m ramp 45-60% LTHR
        (workout.structure.steps[2] as WorkoutSingleStep).target.start == 45
        (workout.structure.steps[2] as WorkoutSingleStep).target.end == 60
        (workout.structure.steps[2] as WorkoutSingleStep).ramp
        // 10m Z2 HR
        (workout.structure.steps[3] as WorkoutSingleStep).target.start == 68
        (workout.structure.steps[3] as WorkoutSingleStep).target.end == 82
        // 10m Z3-Z4 HR
        (workout.structure.steps[4] as WorkoutSingleStep).target.start == 83
        (workout.structure.steps[4] as WorkoutSingleStep).target.end == 105
    }

    def "should parse power workout"() {
        when:
        def workouts = intervalsWorkoutRepository.getPlannedWorkouts(LocalDate.now(), LocalDate.now())

        then:
        workouts.size() == 5

        def workout = findWorkoutWithName("power test", workouts)
        workout.type == TrainingType.BIKE
        workout.structure.target == WorkoutStructure.TargetUnit.FTP_PERCENTAGE
        workout.structure.steps.size() == 5
        // 10m 10-30%
        (workout.structure.steps[0] as WorkoutSingleStep).duration == Duration.ofMinutes(10)
        (workout.structure.steps[0] as WorkoutSingleStep).target.start == 10
        (workout.structure.steps[0] as WorkoutSingleStep).target.end == 30
        // 10m 40% 30-90rpm
        (workout.structure.steps[1] as WorkoutSingleStep).target.start == 39
        (workout.structure.steps[1] as WorkoutSingleStep).target.end == 41
        (workout.structure.steps[1] as WorkoutSingleStep).cadence.start == 30
        (workout.structure.steps[1] as WorkoutSingleStep).cadence.end == 90
        // 10m ramp 10-60%
        (workout.structure.steps[2] as WorkoutSingleStep).target.start == 10
        (workout.structure.steps[2] as WorkoutSingleStep).target.end == 60
        (workout.structure.steps[2] as WorkoutSingleStep).ramp
        // 10m Z2 85rpm
        (workout.structure.steps[3] as WorkoutSingleStep).target.start == 56
        (workout.structure.steps[3] as WorkoutSingleStep).target.end == 75
        (workout.structure.steps[3] as WorkoutSingleStep).cadence.start == 85
        (workout.structure.steps[3] as WorkoutSingleStep).cadence.end == 85
        // 10m Z3-Z4
        (workout.structure.steps[4] as WorkoutSingleStep).target.start == 76
        (workout.structure.steps[4] as WorkoutSingleStep).target.end == 105
    }

    def "should parse pace workout"() {
        when:
        def workouts = intervalsWorkoutRepository.getPlannedWorkouts(LocalDate.now(), LocalDate.now())

        then:
        workouts.size() == 5

        def workout = findWorkoutWithName("pace test", workouts)
        workout.type == TrainingType.RUN
        workout.structure.target == WorkoutStructure.TargetUnit.PACE_PERCENTAGE
        workout.structure.steps.size() == 4
        // 10m 70% Pace
        (workout.structure.steps[0] as WorkoutSingleStep).duration == Duration.ofMinutes(10)
        (workout.structure.steps[0] as WorkoutSingleStep).target.start == 68
        (workout.structure.steps[0] as WorkoutSingleStep).target.end == 72
        // 10m 80-110% Pace
        (workout.structure.steps[1] as WorkoutSingleStep).target.start == 80
        (workout.structure.steps[1] as WorkoutSingleStep).target.end == 110
        // 10m Z2 Pace
        (workout.structure.steps[2] as WorkoutSingleStep).target.start == 79
        (workout.structure.steps[2] as WorkoutSingleStep).target.end == 88
        // 10m Z3-Z5 Pace
        (workout.structure.steps[3] as WorkoutSingleStep).target.start == 89
        (workout.structure.steps[3] as WorkoutSingleStep).target.end == 103
    }

    def "should parse virtual ride workout"() {
        when:
        def workouts = intervalsWorkoutRepository.getPlannedWorkouts(LocalDate.now(), LocalDate.now())

        then:
        workouts.size() == 5

        def workout = findWorkoutWithName("virtual ride test", workouts)
        workout.type == TrainingType.VIRTUAL_BIKE
        workout.structure.target == WorkoutStructure.TargetUnit.FTP_PERCENTAGE
        workout.structure.steps.size() == 5
    }

    def "should parse other workout"() {
        when:
        def workouts = intervalsWorkoutRepository.getPlannedWorkouts(LocalDate.now(), LocalDate.now())

        then:
        workouts.size() == 5

        def workout = findWorkoutWithName("other test", workouts)
        workout.type == TrainingType.UNKNOWN
        workout.duration == Duration.ofMinutes(45)
        workout.load == 32
        workout.structure == null
    }

    def findWorkoutWithName(name, List<Workout> workouts) {
        workouts.stream().filter { it.getTitle() == name }.findFirst().orElseThrow()
    }
}

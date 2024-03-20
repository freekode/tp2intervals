package org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout

import org.freekode.tp2intervals.config.SpringIT
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.config.AppConfigurationRepository
import org.freekode.tp2intervals.domain.config.UpdateConfigurationRequest
import org.freekode.tp2intervals.domain.workout.structure.WorkoutSingleStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure
import org.springframework.beans.factory.annotation.Autowired

import java.time.Duration

class TrainerRoadWorkoutRepositoryTest extends SpringIT {
    @Autowired
    TrainerRoadWorkoutRepository trainerRoadWorkoutRepository

    @Autowired
    AppConfigurationRepository appConfigurationRepository

    def setup() {
        appConfigurationRepository.updateConfig(new UpdateConfigurationRequest(
                Map.of(
                        "trainer-road.auth-cookie", "my-cookie",
                )
        ))
    }

    def "should parse simple workout"() {
        when:
        def workout = trainerRoadWorkoutRepository.getWorkoutsFromLibrary("obelisk")

        then:
        workout.type == TrainingType.VIRTUAL_BIKE
        workout.structure.target == WorkoutStructure.TargetUnit.FTP_PERCENTAGE
        workout.structure.steps.size() == 11
        (workout.structure.steps[0] as WorkoutSingleStep).duration == Duration.ofMinutes(5)
        (workout.structure.steps[0] as WorkoutSingleStep).target.start == 50
        (workout.structure.steps[0] as WorkoutSingleStep).target.end == 50
        (workout.structure.steps[1] as WorkoutSingleStep).duration == Duration.ofMinutes(3)
        (workout.structure.steps[1] as WorkoutSingleStep).target.start == 60
        (workout.structure.steps[1] as WorkoutSingleStep).target.end == 60
        (workout.structure.steps[2] as WorkoutSingleStep).duration == Duration.ofMinutes(13)
        (workout.structure.steps[2] as WorkoutSingleStep).target.start == 50
        (workout.structure.steps[2] as WorkoutSingleStep).target.end == 50
    }

    def "should parse complex workout"() {
        when:
        def workout = trainerRoadWorkoutRepository.getWorkoutsFromLibrary("abney")

        then:
        workout.type == TrainingType.VIRTUAL_BIKE
        workout.structure.target == WorkoutStructure.TargetUnit.FTP_PERCENTAGE
        workout.structure.steps.size() == 23
        (workout.structure.steps[0] as WorkoutSingleStep).duration == Duration.ofMinutes(4)
        (workout.structure.steps[0] as WorkoutSingleStep).target.start == 50
        (workout.structure.steps[0] as WorkoutSingleStep).target.end == 50
        (workout.structure.steps[1] as WorkoutSingleStep).duration == Duration.ofMinutes(2)
        (workout.structure.steps[1] as WorkoutSingleStep).target.start == 55
        (workout.structure.steps[1] as WorkoutSingleStep).target.end == 55
        (workout.structure.steps[2] as WorkoutSingleStep).duration == Duration.ofMinutes(2)
        (workout.structure.steps[2] as WorkoutSingleStep).target.start == 60
        (workout.structure.steps[2] as WorkoutSingleStep).target.end == 60
    }
}

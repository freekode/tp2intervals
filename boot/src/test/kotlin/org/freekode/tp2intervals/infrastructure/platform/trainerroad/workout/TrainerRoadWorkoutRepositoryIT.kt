package org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout

import config.SpringITConfig
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.config.AppConfigurationRepository
import org.freekode.tp2intervals.domain.workout.structure.StepStructure
import org.freekode.tp2intervals.domain.workout.structure.WorkoutSingleStep
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class TrainerRoadWorkoutRepositoryIT : SpringITConfig() {
    @Autowired
    lateinit var trainerRoadWorkoutRepository: TrainerRoadWorkoutRepository

    @Autowired
    lateinit var appConfigurationRepository: AppConfigurationRepository

    @Test
    fun `should parse simple workout`() {
        // when
        val data = ExternalData(null, null, "simple")
        val workout = trainerRoadWorkoutRepository.getWorkoutFromLibrary(data)

        // then
        assertTrue(workout.details.type == TrainingType.VIRTUAL_BIKE)
        assertTrue(workout.structure!!.target == StepStructure.TargetUnit.FTP_PERCENTAGE)
        assertTrue(workout.structure!!.steps.size == 11)
        assertTrue((workout.structure!!.steps[0] as WorkoutSingleStep).length.value == 5L * 60)
        assertTrue((workout.structure!!.steps[0] as WorkoutSingleStep).target.start == 50)
        assertTrue((workout.structure!!.steps[0] as WorkoutSingleStep).target.end == 50)
        assertTrue((workout.structure!!.steps[1] as WorkoutSingleStep).length.value == 3L * 60)
        assertTrue((workout.structure!!.steps[1] as WorkoutSingleStep).target.start == 60)
        assertTrue((workout.structure!!.steps[1] as WorkoutSingleStep).target.end == 60)
        assertTrue((workout.structure!!.steps[2] as WorkoutSingleStep).length.value == 13L * 60)
        assertTrue((workout.structure!!.steps[2] as WorkoutSingleStep).target.start == 50)
        assertTrue((workout.structure!!.steps[2] as WorkoutSingleStep).target.end == 50)
    }

    @Test
    fun `should parse complex workout`() {
        // when
        val data = ExternalData(null, null, "complex")
        val workout = trainerRoadWorkoutRepository.getWorkoutFromLibrary(data)

        // then
        assertTrue(workout.details.type == TrainingType.VIRTUAL_BIKE)
        assertTrue(workout.structure!!.target == StepStructure.TargetUnit.FTP_PERCENTAGE)
        assertTrue(workout.structure!!.steps.size == 23)
        assertTrue((workout.structure!!.steps[0] as WorkoutSingleStep).length.value == 4L * 60)
        assertTrue((workout.structure!!.steps[0] as WorkoutSingleStep).target.start == 50)
        assertTrue((workout.structure!!.steps[0] as WorkoutSingleStep).target.end == 50)
        assertTrue((workout.structure!!.steps[1] as WorkoutSingleStep).length.value == 2L * 60)
        assertTrue((workout.structure!!.steps[1] as WorkoutSingleStep).target.start == 55)
        assertTrue((workout.structure!!.steps[1] as WorkoutSingleStep).target.end == 55)
        assertTrue((workout.structure!!.steps[2] as WorkoutSingleStep).length.value == 2L * 60)
        assertTrue((workout.structure!!.steps[2] as WorkoutSingleStep).target.start == 60)
        assertTrue((workout.structure!!.steps[2] as WorkoutSingleStep).target.end == 60)
    }

    @Test
    fun `should print wrong rest api response`() {
        // when
        val data = ExternalData(null, null, "another")
        val workout = trainerRoadWorkoutRepository.getWorkoutFromLibrary(data)

        // then
        assertTrue(workout.details.type == TrainingType.VIRTUAL_BIKE)
        assertTrue(workout.structure!!.target == StepStructure.TargetUnit.FTP_PERCENTAGE)
        assertTrue(workout.structure!!.steps.isNotEmpty())
    }
}

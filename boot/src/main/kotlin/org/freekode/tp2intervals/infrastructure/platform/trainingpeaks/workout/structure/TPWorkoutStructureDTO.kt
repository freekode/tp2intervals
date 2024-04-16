package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure

import org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure

class TPWorkoutStructureDTO(
    val structure: List<TPStructureStepDTO>,
    val primaryLengthMetric: String?, // distance, duration
    val primaryIntensityMetric: String,
) {
    fun toTargetUnit(): WorkoutStructure.TargetUnit = TPTargetMapper.getByIntensity(primaryIntensityMetric)
}

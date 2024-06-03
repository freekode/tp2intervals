package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure

import org.freekode.tp2intervals.domain.workout.structure.StepStructure

class TPWorkoutStructureDTO(
    val structure: List<TPStructureStepDTO>,
    val primaryLengthMetric: String?, // distance, duration
    val primaryIntensityMetric: String?,
) {
    fun toTargetUnit(): StepStructure.TargetUnit = TPTargetMapper.getByIntensity(primaryIntensityMetric!!)
}

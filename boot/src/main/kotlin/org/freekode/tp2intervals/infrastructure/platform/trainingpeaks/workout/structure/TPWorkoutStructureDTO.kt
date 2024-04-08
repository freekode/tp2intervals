package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure

import org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure

class TPWorkoutStructureDTO(
    var structure: List<TPStructureStepDTO>,
    var primaryLengthMetric: String, // distance, duration
    var primaryIntensityMetric: String,
    var primaryIntensityTargetOrRange: String?, // range, target
    var visualizationDistanceUnit: String?
) {
    fun toTargetUnit(): WorkoutStructure.TargetUnit = TPTargetMapper.getByIntensity(primaryIntensityMetric)
}

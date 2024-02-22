package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure

import org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure

class TPWorkoutStructureDTO(
    var structure: List<TPStructureStepDTO>,
    var primaryLengthMetric: LengthMetric,
    var primaryIntensityMetric: String,
    var primaryIntensityTargetOrRange: IntensityTargetOrRange?,
    var visualizationDistanceUnit: String?
) {
    fun toTargetUnit(): WorkoutStructure.TargetUnit = TPTargetMapper.getByIntensity(primaryIntensityMetric)

    enum class LengthMetric {
        distance,
        duration
    }

    enum class IntensityTargetOrRange {
        range,
        target
    }
}

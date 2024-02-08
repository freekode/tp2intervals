package org.freekode.tp2intervals.infrastructure.trainingpeaks.workout.structure

import org.freekode.tp2intervals.domain.workout.WorkoutStepTarget

class TPWorkoutStructureDTO(
    var structure: List<TPStructureStepDTO>,
    var primaryLengthMetric: LengthMetric,
    var primaryIntensityMetric: IntensityMetric,
    var primaryIntensityTargetOrRange: IntensityTargetOrRange?,
    var visualizationDistanceUnit: String?
) {

    enum class LengthMetric {
        distance,
        duration
    }

    enum class IntensityMetric(
        val targetUnit: WorkoutStepTarget.TargetUnit,
    ) {
        percentOfFtp(WorkoutStepTarget.TargetUnit.FTP_PERCENTAGE),
        percentOfThresholdHr(WorkoutStepTarget.TargetUnit.LTHR_PERCENTAGE),
        percentOfThresholdPace(WorkoutStepTarget.TargetUnit.PACE_PERCENTAGE),
        percentOfMaxHr(WorkoutStepTarget.TargetUnit.UNKNOWN),
        rpe(WorkoutStepTarget.TargetUnit.UNKNOWN)
    }

    enum class IntensityTargetOrRange {
        range,
        target
    }
}

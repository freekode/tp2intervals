package org.freekode.tp2intervals.infrastructure.thirdparty.workout.structure

import org.freekode.tp2intervals.domain.workout.WorkoutStepTarget

class ThirdPartyWorkoutStructureDTO(
    var structure: List<StructureStepDTO>,
    var primaryLengthMetric: LengthMetric,
    var primaryIntensityMetric: IntensityMetric,
    var primaryIntensityTargetOrRange: IntensityTargetOrRange?,
    var visualizationDistanceUnit: String?
) {

    enum class LengthMetric {
        distance,
        duration
    }

    enum class IntensityMetric(val targetUnit: WorkoutStepTarget.TargetUnit) {
        percentOfFtp(WorkoutStepTarget.TargetUnit.FTP_PERCENTAGE),
        percentOfThresholdHr(WorkoutStepTarget.TargetUnit.FTP_PERCENTAGE),
        percentOfMaxHr(WorkoutStepTarget.TargetUnit.FTP_PERCENTAGE),
        percentOfThresholdPace(WorkoutStepTarget.TargetUnit.FTP_PERCENTAGE),
        rpe(WorkoutStepTarget.TargetUnit.FTP_PERCENTAGE)
    }

    enum class IntensityTargetOrRange {
        range,
        target
    }
}

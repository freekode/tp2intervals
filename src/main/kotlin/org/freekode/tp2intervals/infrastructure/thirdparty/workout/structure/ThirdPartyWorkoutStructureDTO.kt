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

    enum class IntensityMetric(
        val targetUnit: WorkoutStepTarget.TargetUnit,
        val targetType: WorkoutStepTarget.TargetType
    ) {
        percentOfFtp(WorkoutStepTarget.TargetUnit.FTP_PERCENTAGE, WorkoutStepTarget.TargetType.POWER),
        percentOfThresholdHr(WorkoutStepTarget.TargetUnit.LTHR_PERCENTAGE, WorkoutStepTarget.TargetType.HR),
        percentOfMaxHr(WorkoutStepTarget.TargetUnit.FTP_PERCENTAGE, WorkoutStepTarget.TargetType.HR),

        // TODO them
        percentOfThresholdPace(WorkoutStepTarget.TargetUnit.FTP_PERCENTAGE, WorkoutStepTarget.TargetType.POWER),
        rpe(WorkoutStepTarget.TargetUnit.FTP_PERCENTAGE, WorkoutStepTarget.TargetType.POWER)
    }

    enum class IntensityTargetOrRange {
        range,
        target
    }
}

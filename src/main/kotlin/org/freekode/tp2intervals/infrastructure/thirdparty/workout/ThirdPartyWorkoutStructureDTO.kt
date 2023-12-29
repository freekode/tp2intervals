package org.freekode.tp2intervals.infrastructure.thirdparty.workout

class ThirdPartyWorkoutStructureDTO(
    var structure: List<StructureDTO>,
    var primaryLengthMetric: String,
    var primaryIntensityMetric: String,
    var primaryIntensityTargetOrRange: String,
) {
    class StructureDTO(
        var type: StructureType,
        var length: LengthDTO,
        var steps: List<StepDTO>,
    )

    class StepDTO(
        var name: String,
        var length: LengthDTO,
        var targets: List<TargetDTO>,
        var intensityClass: String?,
        var openDuration: Boolean?
    )

    class LengthDTO(
        var value: Int,
        var unit: LengthUnit,
    )

    enum class LengthUnit {
        step,
        second,
        repetition,
    }

    class TargetDTO(
        var minValue: Int,
        var maxValue: Int,
        var unit: String?,
    )

    enum class StructureType {
        step,
        repetition,
    }
}

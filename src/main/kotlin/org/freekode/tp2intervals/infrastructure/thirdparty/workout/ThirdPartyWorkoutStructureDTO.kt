package org.freekode.tp2intervals.infrastructure.thirdparty.workout

import org.freekode.tp2intervals.domain.workout.IntensityType

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
        var intensityClass: IntensityClass?,
        var openDuration: Boolean?
    )

    class LengthDTO(
        var value: Int,
        var unit: LengthUnit,
    )

    class TargetDTO(
        var minValue: Int,
        var maxValue: Int,
        var unit: String?,
    )

    enum class IntensityClass(val type: IntensityType) {
        warmUp(IntensityType.WARM_UP),
        active(IntensityType.ACTIVE),
        rest(IntensityType.REST),
        coolDown(IntensityType.COOL_DOWN);

        companion object {
            fun findByIntensityType(type: IntensityType): IntensityClass {
                return entries.find { it.type == type }!!
            }
        }
    }

    enum class LengthUnit {
        step,
        second,
        repetition,
    }

    enum class StructureType {
        step,
        repetition,
    }
}

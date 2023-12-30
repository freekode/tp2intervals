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
    ) {
        companion object {
            fun singleStep(stepDTO: StepDTO): StructureDTO =
                StructureDTO(StructureType.step, LengthDTO(1, LengthUnit.repetition), listOf(stepDTO))

            fun multiStep(stepDTOs: List<StepDTO>): StructureDTO =
                StructureDTO(
                    StructureType.repetition,
                    LengthDTO.repetitions(stepDTOs.size.toLong()),
                    stepDTOs
                )
        }
    }

    class StepDTO(
        var name: String,
        var length: LengthDTO,
        var targets: List<TargetDTO>,
        var intensityClass: IntensityClass?,
        var openDuration: Boolean?
    )

    class LengthDTO(
        var value: Long,
        var unit: LengthUnit,
    ) {
        companion object {
            fun seconds(value: Long) = LengthDTO(value, LengthUnit.second)
            fun repetitions(value: Long) = LengthDTO(value, LengthUnit.repetition)
        }
    }

    class TargetDTO(
        var minValue: Int,
        var maxValue: Int,
        var unit: TargetUnit?,
    ) {
        companion object {
            fun powerTarget(minValue: Int, maxValue: Int): TargetDTO = TargetDTO(minValue, maxValue, null)
            fun cadenceTarget(minValue: Int, maxValue: Int): TargetDTO =
                TargetDTO(minValue, maxValue, TargetUnit.roundOrStridePerMinute)
        }
    }

    enum class TargetUnit {
        roundOrStridePerMinute
    }

    enum class IntensityClass(val type: IntensityType) {
        warmUp(IntensityType.WARM_UP), active(IntensityType.ACTIVE), rest(IntensityType.REST), coolDown(IntensityType.COOL_DOWN);

        companion object {
            fun findByIntensityType(type: IntensityType): IntensityClass {
                return entries.find { it.type == type }!!
            }
        }
    }

    enum class LengthUnit {
        step, second, repetition,
    }

    enum class StructureType {
        step, repetition,
    }
}

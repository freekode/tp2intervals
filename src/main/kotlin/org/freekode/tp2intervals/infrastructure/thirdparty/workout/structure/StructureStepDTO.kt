package org.freekode.tp2intervals.infrastructure.thirdparty.workout.structure

class StructureStepDTO(
    var type: StructureType,
    var length: LengthDTO,
    var steps: List<StepDTO>,
) {
    companion object {
        fun singleStep(stepDTO: StepDTO): StructureStepDTO =
            StructureStepDTO(
                StructureType.step,
                LengthDTO(1, LengthDTO.LengthUnit.repetition),
                listOf(stepDTO)
            )

        fun multiStep(repetitions: Int, stepDTOs: List<StepDTO>): StructureStepDTO =
            StructureStepDTO(
                StructureType.repetition,
                LengthDTO.repetitions(repetitions.toLong()),
                stepDTOs
            )
    }

    enum class StructureType {
        step, repetition,
    }
}

package org.freekode.tp2intervals.infrastructure.trainingpeaks.workout.structure

class TPStructureStepDTO(
    var type: StructureType,
    var length: TPLengthDTO,
    var steps: List<TPStepDTO>,
    val begin: Long?,
    val end: Long?
) {
    companion object {
        fun singleStep(TPStepDTO: TPStepDTO): TPStructureStepDTO =
            TPStructureStepDTO(
                StructureType.step,
                TPLengthDTO(1, TPLengthDTO.LengthUnit.repetition),
                listOf(TPStepDTO),
                null,
                null
            )

        fun multiStep(repetitions: Int, TPStepDTOS: List<TPStepDTO>): TPStructureStepDTO =
            TPStructureStepDTO(
                StructureType.repetition,
                TPLengthDTO.repetitions(repetitions.toLong()),
                TPStepDTOS,
                null,
                null
            )
    }

    enum class StructureType {
        step, repetition, rampUp
    }
}

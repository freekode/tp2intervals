package org.freekode.tp2intervals.infrastructure.trainingpeaks.workout.structure

class TPStructureStepDTO(
    var type: StructureType,
    var length: TPLengthDTO,
    var steps: List<TPStepDTO>,
    val begin: Long?,
    val end: Long?
) {
    companion object {
        fun singleStep(tPStepDTO: TPStepDTO): TPStructureStepDTO =
            TPStructureStepDTO(
                StructureType.step,
                TPLengthDTO.single(),
                listOf(tPStepDTO),
                null,
                null
            )

        fun multiStep(repetitions: Int, tPStepDTOS: List<TPStepDTO>): TPStructureStepDTO =
            TPStructureStepDTO(
                StructureType.repetition,
                TPLengthDTO.repetitions(repetitions.toLong()),
                tPStepDTOS,
                null,
                null
            )
    }

    enum class StructureType {
        step, repetition, rampUp, rampDown
    }
}

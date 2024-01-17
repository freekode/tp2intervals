package org.freekode.tp2intervals.infrastructure.trainingpeaks.workout.structure

class TPStructureStepDTO(
    var type: StructureType,
    var length: TPLengthDTO,
    var steps: List<TPStepDTO>,
    val begin: Long? = null,
    val end: Long? = null
) {
    companion object {
        fun singleStep(stepDTO: TPStepDTO): TPStructureStepDTO =
            TPStructureStepDTO(
                StructureType.step,
                TPLengthDTO.single(),
                listOf(stepDTO),
            )

        fun multiStep(repetitions: Int, stepDTOs: List<TPStepDTO>): TPStructureStepDTO =
            TPStructureStepDTO(
                StructureType.repetition,
                TPLengthDTO.repetitions(repetitions.toLong()),
                stepDTOs,
            )

        fun rampUp(stepDTOs: List<TPStepDTO>): TPStructureStepDTO =
            TPStructureStepDTO(
                StructureType.rampUp,
                TPLengthDTO.single(),
                stepDTOs,
            )

        fun rampDown(stepDTOs: List<TPStepDTO>): TPStructureStepDTO =
            TPStructureStepDTO(
                StructureType.rampDown,
                TPLengthDTO.single(),
                stepDTOs,
            )

    }

    enum class StructureType {
        step, repetition, rampUp, rampDown
    }
}

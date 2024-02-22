package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure

class TPStructureStepDTO(
    var type: StructureType,
    var length: org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure.TPLengthDTO,
    var steps: List<TPStepDTO>,
    val begin: Long? = null,
    val end: Long? = null
) {
    companion object {
        fun singleStep(stepDTO: TPStepDTO): TPStructureStepDTO =
            TPStructureStepDTO(
                StructureType.step,
                org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure.TPLengthDTO.single(),
                listOf(stepDTO),
            )

        fun multiStep(repetitions: Int, stepDTOs: List<TPStepDTO>): TPStructureStepDTO =
            TPStructureStepDTO(
                StructureType.repetition,
                org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure.TPLengthDTO.repetitions(repetitions.toLong()),
                stepDTOs,
            )

        fun rampUp(stepDTOs: List<TPStepDTO>): TPStructureStepDTO =
            TPStructureStepDTO(
                StructureType.rampUp,
                org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure.TPLengthDTO.single(),
                stepDTOs,
            )

        fun rampDown(stepDTOs: List<TPStepDTO>): TPStructureStepDTO =
            TPStructureStepDTO(
                StructureType.rampDown,
                org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure.TPLengthDTO.single(),
                stepDTOs,
            )

    }

    enum class StructureType {
        step, repetition, rampUp, rampDown
    }
}

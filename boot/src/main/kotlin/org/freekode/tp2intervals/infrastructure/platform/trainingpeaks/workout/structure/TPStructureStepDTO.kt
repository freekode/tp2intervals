package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure

class TPStructureStepDTO(
    val type: String?, // step, repetition, rampUp, rampDown
    val length: TPLengthDTO?,
    val steps: List<TPStepDTO> = listOf(),
) {
    companion object {
        fun singleStep(stepDTO: TPStepDTO): TPStructureStepDTO =
            TPStructureStepDTO("step", TPLengthDTO.single(), listOf(stepDTO))

        fun multiStep(repetitions: Int, stepDTOs: List<TPStepDTO>): TPStructureStepDTO =
            TPStructureStepDTO("repetition", TPLengthDTO.repetitions(repetitions.toLong()), stepDTOs)

        fun rampUp(stepDTOs: List<TPStepDTO>): TPStructureStepDTO =
            TPStructureStepDTO("rampUp", TPLengthDTO.single(), stepDTOs)

        fun rampDown(stepDTOs: List<TPStepDTO>): TPStructureStepDTO =
            TPStructureStepDTO("rampDown", TPLengthDTO.single(), stepDTOs)

    }
}

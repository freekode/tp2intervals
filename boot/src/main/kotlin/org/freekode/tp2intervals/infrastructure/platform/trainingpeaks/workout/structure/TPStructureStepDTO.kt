package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure

class TPStructureStepDTO(
    var type: String, // step, repetition, rampUp, rampDown
    var length: TPLengthDTO?,
    var steps: List<TPStepDTO>,
    val begin: Long? = null,
    val end: Long? = null
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

package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure

import org.freekode.tp2intervals.domain.workout.structure.StepIntensityType

class TPStepDTO(
    var name: String,
    var length: TPLengthDTO,
    var targets: List<TPTargetDTO>,
    var intensityClass: IntensityClass?,
    var openDuration: Boolean?
) {
    enum class IntensityClass(val type: StepIntensityType) {
        warmUp(StepIntensityType.WARM_UP),
        active(StepIntensityType.ACTIVE),
        rest(StepIntensityType.REST),
        coolDown(StepIntensityType.COOL_DOWN);

        companion object {
            fun findByIntensityType(type: StepIntensityType): IntensityClass {
                return entries.find { it.type == type }!!
            }
        }
    }
}

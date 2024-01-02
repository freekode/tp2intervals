package org.freekode.tp2intervals.infrastructure.thirdparty.workout.structure

import org.freekode.tp2intervals.domain.workout.StepIntensityType

class StepDTO(
    var name: String,
    var length: LengthDTO,
    var targets: List<TargetDTO>,
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

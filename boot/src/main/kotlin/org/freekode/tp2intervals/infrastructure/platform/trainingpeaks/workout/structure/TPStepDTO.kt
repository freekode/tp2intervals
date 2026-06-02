package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure

import org.freekode.tp2intervals.domain.workout.structure.StepTarget

class TPStepDTO(
    var name: String?,
    var notes: String?,
    var length: TPLengthDTO?,
    var targets: List<TPTargetDTO> = listOf(),
) {

    constructor(name: String?, length: TPLengthDTO?, targets: List<TPTargetDTO>) :
            this(name, null, length, targets)

    fun toMainTarget(): StepTarget {
        val target = targets.first { it.unit == null }
        return StepTarget(
            target.minValue ?: target.maxValue!!,
            target.maxValue ?: target.minValue!!,
        )
    }

    fun toSecondaryTarget(): StepTarget? {
        return targets
            .firstOrNull { it.unit != null }
            ?.let { StepTarget(it.minValue!!, it.maxValue!!) }
    }
}

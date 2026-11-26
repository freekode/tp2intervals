package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure

import org.freekode.tp2intervals.domain.workout.structure.StepTarget

class TPStepDTO(
    var name: String?,
    var length: TPLengthDTO?,
    var targets: List<TPTargetDTO> = listOf(),
) {
    fun toMainTarget(): StepTarget {
        val target = targets.firstOrNull { it.unit == null }
            ?: throw IllegalArgumentException("Step has no primary target")
        val value = requireNotNull(target.minValue ?: target.maxValue) { "Primary target has no value" }
        return StepTarget(
            target.minValue ?: value,
            target.maxValue ?: value,
        )
    }

    fun toSecondaryTarget(): StepTarget? {
        return targets
            .firstOrNull { it.unit != null }
            ?.let {
                val value = requireNotNull(it.minValue ?: it.maxValue) { "Secondary target has no value" }
                StepTarget(it.minValue ?: value, it.maxValue ?: value)
            }
    }
}

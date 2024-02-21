package org.freekode.tp2intervals.infrastructure.intervalsicu.workout

import org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure
import org.freekode.tp2intervals.infrastructure.PlatformException

class IntervalsWorkoutDocDTO(
    val duration: Long?,
    val ftp: Int?,
    val lthr: Int?,
    val threshold_pace: Float?,
    val pace_units: String?,
    val steps: List<WorkoutStepDTO>,
    val target: String
) {
    private val targetMap = mapOf(
        "POWER" to WorkoutStructure.TargetUnit.FTP_PERCENTAGE,
        "HR" to WorkoutStructure.TargetUnit.LTHR_PERCENTAGE,
        "PACE" to WorkoutStructure.TargetUnit.PACE_PERCENTAGE,
    )

    fun mapTarget(): WorkoutStructure.TargetUnit {
        return targetMap[target] ?: throw PlatformException("Cant convert target - $target")
    }

    class WorkoutStepDTO(
        val text: String?,
        val reps: Int?,
        val duration: Long?,
        val power: StepValueDTO?,
        val _power: ResolvedStepValueDTO?,
        val hr: StepValueDTO?,
        val _hr: ResolvedStepValueDTO?,
        val pace: StepValueDTO?,
        val _pace: ResolvedStepValueDTO?,
        val cadence: StepValueDTO?,
        val steps: List<WorkoutStepDTO>?,
        val warmup: Boolean?,
        val cooldown: Boolean?,
        val ramp: Boolean?
    )

    class StepValueDTO(
        val units: String,
        val value: Int?,
        val start: Int?,
        val end: Int?,
    )

    class ResolvedStepValueDTO(
        val value: Double?,
        val start: Double,
        val end: Double,
    )
}

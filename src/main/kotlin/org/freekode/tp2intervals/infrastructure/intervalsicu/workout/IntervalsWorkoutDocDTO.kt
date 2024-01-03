package org.freekode.tp2intervals.infrastructure.intervalsicu.workout

import org.freekode.tp2intervals.domain.workout.WorkoutStepTarget
import java.time.Duration

class IntervalsWorkoutDocDTO(
    val steps: List<WorkoutStepDTO>,
    val duration: Long?,
) {
    class WorkoutStepDTO(
        val text: String?,
        val reps: Int?,
        val duration: Long?,
        val power: StepValueDTO?,
        val cadence: StepValueDTO?,
        val hr: StepValueDTO?,
        val steps: List<WorkoutStepDTO>?,
        val warmup: Boolean?,
        val cooldown: Boolean?
    ) {
        companion object {
            fun singleStep(
                text: String?, duration: Duration, power: StepValueDTO?, cadence: StepValueDTO?,
                hr: StepValueDTO?, warmup: Boolean?, cooldown: Boolean?
            ) = WorkoutStepDTO(text, null, duration.seconds, power, cadence, hr, null, warmup, cooldown)

            fun multiStep(text: String?, reps: Int, steps: List<WorkoutStepDTO>) =
                WorkoutStepDTO(text, reps, null, null, null, null, steps, null, null)
        }
    }

    class StepValueDTO(
        val units: String,
        val value: Int?,
        val start: Int?,
        val end: Int?,
    ) {
        constructor(stepTarget: WorkoutStepTarget) : this(
            getUnit(stepTarget.unit),
            null,
            stepTarget.min,
            stepTarget.max
        )

        companion object {
            private val unitsMap = mapOf(
                "%ftp" to WorkoutStepTarget.TargetUnit.FTP_PERCENTAGE,
                "%lthr" to WorkoutStepTarget.TargetUnit.LTHR_PERCENTAGE,
                "rpm" to WorkoutStepTarget.TargetUnit.RPM,
            )

            fun getUnit(targetUnit: WorkoutStepTarget.TargetUnit) =
                unitsMap.filterValues { it == targetUnit }.keys.iterator().next()
        }

        fun mapTargetUnit(): WorkoutStepTarget.TargetUnit = unitsMap[units]!!
    }
}

package org.freekode.tp2intervals.infrastructure.intervalsicu.workout

import org.freekode.tp2intervals.domain.workout.WorkoutStepTarget

class IntervalsWorkoutDocDTO(
    val steps: List<WorkoutStepDTO>,
    val duration: Long?,
    val zoneTimes: List<IntervalsWorkoutZoneDTO>?
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
        val cooldown: Boolean?,
        val ramp: Boolean?
    )

    class StepValueDTO(
        val units: String,
        val value: Int?,
        val start: Int?,
        val end: Int?,
    ) {
        companion object {
            private val unitsMap = mapOf(
                "%ftp" to WorkoutStepTarget.TargetUnit.FTP_PERCENTAGE,
                "%lthr" to WorkoutStepTarget.TargetUnit.LTHR_PERCENTAGE,
                "rpm" to WorkoutStepTarget.TargetUnit.RPM,
            )
        }

        fun mapTargetUnit(): WorkoutStepTarget.TargetUnit =
            unitsMap[units] ?: throw RuntimeException("Can't handle units for $units")
    }
}

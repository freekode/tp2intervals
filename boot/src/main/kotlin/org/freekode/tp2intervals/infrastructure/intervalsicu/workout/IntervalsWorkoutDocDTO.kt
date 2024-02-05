package org.freekode.tp2intervals.infrastructure.intervalsicu.workout

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
        val hr: StepValueDTO?,
        val pace: StepValueDTO?,
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

    class IntervalsWorkoutZoneDTO(
        val id: String,
        val max: Int?,
        val name: String?,
        val zone: Int?,
        val percentRange: String?,
    ) {
        fun getRange(): List<Int> {
            val range = percentRange!!.split(" - ")
                .map { it.dropLast(1).toInt() }
            return listOf(range[0], range[1])
        }
    }
}

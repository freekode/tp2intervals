package org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout

import java.time.Duration
import java.time.LocalDate
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.workout.structure.WorkoutSingleStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStepTarget
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure

class TRWorkoutConverter(
    trWorkoutResponseDTO: TRWorkoutResponseDTO
) {
    private val trWorkout: TRWorkoutResponseDTO.TRWorkout = trWorkoutResponseDTO.workout

    fun toWorkout(): Workout {
        val steps = convertSteps(trWorkout.intervalData)

        return Workout(
            LocalDate.now(),
            if (trWorkout.details.isOutside) TrainingType.BIKE else TrainingType.VIRTUAL_BIKE,
            trWorkout.details.workoutName,
            trWorkout.details.workoutDescription,
            Duration.ofMinutes(trWorkout.details.duration.toLong()),
            trWorkout.details.tss,
            WorkoutStructure(WorkoutStructure.TargetUnit.FTP_PERCENTAGE, steps),
            ExternalData.empty().withTrainerRoad(trWorkout.details.id.toString())
        )
    }

    private fun convertSteps(intervals: List<TRWorkoutResponseDTO.IntervalsDataDTO>): List<WorkoutStep> {
        val steps = mutableListOf<WorkoutStep>()

        for (interval in intervals) {
            if (interval.name == "Workout") {
                continue
            }
            val duration = Duration.ofSeconds((interval.end - interval.start).toLong())
            val ftpPercent = interval.startTargetPowerPercent

            val workoutSingleStep =
                WorkoutSingleStep(interval.name, duration, WorkoutStepTarget(ftpPercent, ftpPercent), null, false)
            steps.add(workoutSingleStep)
        }
        return steps
    }
}

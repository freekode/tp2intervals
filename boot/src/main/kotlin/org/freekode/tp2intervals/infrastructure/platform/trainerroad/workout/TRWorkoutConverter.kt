package org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout

import java.time.Duration
import java.time.LocalDate
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutExternalData
import org.freekode.tp2intervals.domain.workout.WorkoutSingleStep
import org.freekode.tp2intervals.domain.workout.WorkoutStep
import org.freekode.tp2intervals.domain.workout.WorkoutStepTarget

class TRWorkoutConverter(
    trWorkoutResponseDTO: TRWorkoutResponseDTO
) {
    private val trWorkout: TRWorkoutResponseDTO.TRWorkout = trWorkoutResponseDTO.Workout

    fun toWorkout(): Workout {
        val steps = convertSteps(trWorkout.intervalData)

        return Workout(
            LocalDate.now(),
            if (trWorkout.Details.IsOutside) TrainingType.BIKE else TrainingType.VIRTUAL_BIKE,
            trWorkout.Details.WorkoutName,
            trWorkout.Details.WorkoutDescription,
            Duration.ofMinutes(trWorkout.Details.Duration.toLong()),
            trWorkout.Details.TSS,
            steps,
            WorkoutExternalData.trainerRoad(trWorkout.Details.Id.toString())
        )
    }

    private fun convertSteps(intervals: List<TRWorkoutResponseDTO.IntervalsDataDTO>): List<WorkoutStep> {
        val steps = mutableListOf<WorkoutStep>()

        for (interval in intervals) {
            if (interval.Name == "Workout") {
                continue
            }
            val duration = Duration.ofSeconds((interval.End - interval.Start).toLong())
            val ftpPercent = interval.StartTargetPowerPercent

            val workoutSingleStep = WorkoutSingleStep(interval.Name, duration, getStepTarget(ftpPercent), null, false)
            steps.add(workoutSingleStep)
        }
        return steps
    }

    private fun getStepTarget(ftpPercent: Int) =
        WorkoutStepTarget(WorkoutStepTarget.TargetUnit.FTP_PERCENTAGE, ftpPercent, ftpPercent)
}

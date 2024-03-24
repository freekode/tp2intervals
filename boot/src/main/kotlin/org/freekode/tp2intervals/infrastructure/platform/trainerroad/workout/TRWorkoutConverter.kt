package org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout

import java.time.Duration
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.domain.workout.structure.WorkoutSingleStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStepTarget
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure

class TRWorkoutConverter {
    fun toWorkout(trWorkoutResponseDTO: TRWorkoutResponseDTO): Workout {
        val trWorkout: TRWorkoutResponseDTO.TRWorkout = trWorkoutResponseDTO.workout

        val steps = convertSteps(trWorkout.intervalData)

        return Workout(
            WorkoutDetails(
                if (trWorkout.details.isOutside) TrainingType.BIKE else TrainingType.VIRTUAL_BIKE,
                trWorkout.details.workoutName,
                trWorkout.details.workoutDescription,
                Duration.ofMinutes(trWorkout.details.duration.toLong()),
                trWorkout.details.tss,
                ExternalData.empty().withTrainerRoad(trWorkout.details.id)
            ),
            null,
            WorkoutStructure(WorkoutStructure.TargetUnit.FTP_PERCENTAGE, steps),
        )
    }

    fun toWorkoutDetails(trWorkout: TRFindWorkoutsResponseDTO.TRWorkout): WorkoutDetails {
        return WorkoutDetails(
            if (trWorkout.isOutside) TrainingType.BIKE else TrainingType.VIRTUAL_BIKE,
            trWorkout.workoutName,
            trWorkout.workoutDescription,
            Duration.ofMinutes(trWorkout.duration.toLong()),
            trWorkout.tss,
            ExternalData.empty().withTrainerRoad(trWorkout.id)
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

            val name = if (interval.name == "Fake") "Step" else interval.name

            val workoutSingleStep =
                WorkoutSingleStep(name, duration, WorkoutStepTarget(ftpPercent, ftpPercent), null, false)
            steps.add(workoutSingleStep)
        }
        return steps
    }
}

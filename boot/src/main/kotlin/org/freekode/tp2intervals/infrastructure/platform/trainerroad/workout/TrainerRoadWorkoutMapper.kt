package org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout

import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.domain.workout.structure.*
import java.time.Duration

class TrainerRoadWorkoutMapper {
    fun toWorkout(trWorkoutResponseDTO: TRWorkoutResponseDTO, removeHtmlTags: Boolean): Workout {
        val trWorkout: TRWorkoutResponseDTO.TRWorkout = trWorkoutResponseDTO.workout
        val steps = convertSteps(trWorkout.intervalData)
        return Workout(
            toWorkoutDetails(trWorkout.details, removeHtmlTags),
            null,
            WorkoutStructure(WorkoutStructure.TargetUnit.FTP_PERCENTAGE, steps),
        )
    }

    fun toWorkoutDetails(detailsDTO: TrainerRoadWorkoutDetailsDTO, removeHtmlTags: Boolean): WorkoutDetails {
        return WorkoutDetails(
            if (detailsDTO.isOutside) TrainingType.BIKE else TrainingType.VIRTUAL_BIKE,
            detailsDTO.workoutName,
            getDescription(detailsDTO.workoutDescription, removeHtmlTags),
            Duration.ofMinutes(detailsDTO.duration.toLong()),
            detailsDTO.tss,
            ExternalData.empty().withTrainerRoad(detailsDTO.id)
        )
    }

    private fun convertSteps(intervals: List<TRWorkoutResponseDTO.IntervalsDataDTO>): List<WorkoutStep> {
        val steps = mutableListOf<WorkoutStep>()

        for (interval in intervals) {
            if (interval.name == "Workout") {
                continue
            }
            val stepLength = StepLength.seconds((interval.end - interval.start).toLong())
            val ftpPercent = interval.startTargetPowerPercent

            val name = if (interval.name == "Fake") "Step" else interval.name

            val workoutSingleStep =
                WorkoutSingleStep(name, stepLength, StepTarget(ftpPercent, ftpPercent), null, false)
            steps.add(workoutSingleStep)
        }
        return steps
    }

    private fun getDescription(description: String, removeHtmlTags: Boolean): String =
        if (removeHtmlTags) {
            description.replace("<[^>]*>".toRegex(), " ").replace("&" + "nbsp;", " ").replace("\\s+".toRegex(), " ")
        } else {
            description
        }.trim()
}

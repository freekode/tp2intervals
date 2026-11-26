package org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout

import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.domain.workout.structure.*
import org.freekode.tp2intervals.infrastructure.PlatformException
import java.time.Duration
import kotlin.math.roundToInt

class TrainerRoadWorkoutMapper {
    fun toWorkout(trWorkoutResponseDTO: TRWorkoutResponseDTO, removeHtmlTags: Boolean): Workout {
        val trWorkout: TRWorkoutResponseDTO.TRWorkout = trWorkoutResponseDTO.workout
        val steps = convertSteps(trWorkout.intervalData, trWorkout.workoutData)
        return Workout(
            toWorkoutDetails(trWorkout.details, removeHtmlTags),
            null,
            if (steps.isEmpty()) null else
                WorkoutStructure(WorkoutStructure.TargetUnit.FTP_PERCENTAGE, steps),
        )
    }

    fun toWorkoutDetails(detailsDTO: TrainerRoadWorkoutDetailsDTO, removeHtmlTags: Boolean): WorkoutDetails {
        return WorkoutDetails(
            if (detailsDTO.isOutside) TrainingType.BIKE else TrainingType.VIRTUAL_BIKE,
            detailsDTO.workoutName,
            getDescription(detailsDTO, removeHtmlTags),
            Duration.ofMinutes(detailsDTO.duration.toLong()),
            detailsDTO.tss,
            ExternalData.empty().withTrainerRoad(detailsDTO.id)
        )
    }

    private fun convertSteps(
        intervals: List<TRWorkoutResponseDTO.IntervalsDataDTO>,
        workoutData: List<TRWorkoutResponseDTO.WorkoutDataDTO>,
    ): List<WorkoutStep> {
        val steps = mutableListOf<WorkoutStep>()

        for (interval in intervals) {
            if (interval.name == "Workout") {
                continue
            }
            val stepLength = StepLength.seconds((interval.end - interval.start).toLong())
            val ftpPercent = targetAtTick(workoutData, interval.start)
                ?: interval.startTarget?.firstOrNull()?.roundToInt()
                ?: interval.startTargetPowerPercent?.toInt()
                ?: throw PlatformException(
                    Platform.TRAINER_ROAD,
                    "TrainerRoad interval '${interval.name}' has no power target; cannot copy workout",
                )
            val ftpEndPercent = targetAtTick(workoutData, interval.end - 1)
                ?: interval.startTarget?.getOrNull(1)?.roundToInt()
                ?: interval.endTargetPowerPercent?.toInt()
                ?: ftpPercent

            val name = if (interval.name == "Fake") "Step" else interval.name

            val singleStep =
                SingleStep(name, stepLength, StepTarget(ftpPercent, ftpEndPercent), null, false)
            steps.add(singleStep)
        }
        return steps
    }

    private fun targetAtTick(
        workoutData: List<TRWorkoutResponseDTO.WorkoutDataDTO>,
        tick: Double,
    ): Int? {
        return workoutData.firstOrNull { it.tick == tick }?.ftpPercent?.toInt()
    }

    private fun getDescription(details: TrainerRoadWorkoutDetailsDTO, removeHtmlTags: Boolean): String {
        val sections = mutableListOf(formatDescription(details.workoutDescription, removeHtmlTags))
        details.goalDescription?.takeIf { it.isNotBlank() }?.let {
            sections += "Goal: ${formatDescription(it, removeHtmlTags)}"
        }
        val metadata = mutableListOf<String>()
        details.zones.firstOrNull()?.description?.takeIf { it.isNotBlank() }?.let { metadata += "Zone: $it" }
        details.profileName?.takeIf { it.isNotBlank() }?.let { metadata += "Profile: $it" }
        details.workoutDifficultyRating?.let { metadata += "Difficulty: $it" }
        details.workoutLevel?.let { metadata += "Workout level: ${formatDecimal(it)}" }
        details.progression?.text?.takeIf { it.isNotBlank() }?.let { progression ->
            val level = details.levels.firstOrNull { it.isPrimary == true }?.level ?: details.progressionLevel
            metadata += "Progression: $progression${level?.let { " ${formatDecimal(it)}" }.orEmpty()}"
        }
        if (metadata.isNotEmpty()) sections += metadata.joinToString(" | ")
        return sections.joinToString("\n\n").trim()
    }

    private fun formatDescription(description: String, removeHtmlTags: Boolean): String =
        if (removeHtmlTags) {
            description
                .replace("<[^>]*>".toRegex(), " ")
                .replace("&amp;", "&")
                .replace("&nbsp;", " ")
                .replace("\\s+".toRegex(), " ")
        } else {
            description
        }.trim()

    private fun formatDecimal(value: Double): String =
        if (value % 1.0 == 0.0) value.toInt().toString() else String.format("%.2f", value)
}

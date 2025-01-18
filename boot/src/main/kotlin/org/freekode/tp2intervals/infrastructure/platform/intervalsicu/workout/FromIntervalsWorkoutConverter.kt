package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout

import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.domain.workout.structure.MultiStep
import org.freekode.tp2intervals.domain.workout.structure.SingleStep
import org.freekode.tp2intervals.domain.workout.structure.StepLength
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure

class FromIntervalsWorkoutConverter(
    private val eventDTO: IntervalsEventDTO
) {
    private val workoutDoc: IntervalsWorkoutDocDTO? by lazy { eventDTO.workout_doc }

    fun toWorkout(): Workout {
        val workoutsStructure = toWorkoutStructure()

        return Workout(
            WorkoutDetails(
                eventDTO.mapType(),
                eventDTO.name,
                eventDTO.description,
                eventDTO.mapDuration(),
                eventDTO.icu_training_load,
                ExternalData.empty().withIntervals(eventDTO.id.toString()).fromSimpleString(eventDTO.description ?: "")
            ),
            eventDTO.start_date_local.toLocalDate(),
            workoutsStructure,
        )
    }

    private fun toWorkoutStructure(): WorkoutStructure? {
        return workoutDoc?.let { workoutDoc ->
            if (workoutDoc.steps.isNotEmpty()) {
                WorkoutStructure(
                    workoutDoc.mapTarget(),
                    mapToWorkoutSteps(workoutDoc)
                )
            } else {
                null
            }
        }
    }

    private fun mapToWorkoutSteps(workoutDoc: IntervalsWorkoutDocDTO): List<WorkoutStep> {
        return workoutDoc.steps.map {
            if (it.reps != null) {
                mapMultiStep(it)
            } else {
                mapSingleStep(it)
            }
        }
    }

    private fun mapMultiStep(
        stepDTO: IntervalsWorkoutDocDTO.WorkoutStepDTO
    ): MultiStep {
        return MultiStep(
            stepDTO.text,
            stepDTO.reps!!,
            stepDTO.steps!!.map { mapSingleStep(it) }
        )
    }

    private fun mapSingleStep(
        stepDTO: IntervalsWorkoutDocDTO.WorkoutStepDTO
    ): SingleStep {
        val targetMapper = IntervalsToTargetConverter(
            workoutDoc!!.ftp?.toDouble(),
            workoutDoc!!.lthr?.toDouble(),
            workoutDoc!!.threshold_pace?.toDouble()
        )
        val mainTarget = targetMapper.toMainTarget(stepDTO)
        val cadenceTarget = stepDTO.cadence?.let { targetMapper.toCadenceTarget(it) }

        return SingleStep(
            stepDTO.text,
            getStepLength(stepDTO),
            mainTarget,
            cadenceTarget,
            stepDTO.ramp == true
        )
    }

    private fun getStepLength(stepDTO: IntervalsWorkoutDocDTO.WorkoutStepDTO) =
        if (stepDTO.distance != null) {
            StepLength.meters(stepDTO.distance)
        } else {
            StepLength.seconds(stepDTO.duration ?: 600)
        }
}

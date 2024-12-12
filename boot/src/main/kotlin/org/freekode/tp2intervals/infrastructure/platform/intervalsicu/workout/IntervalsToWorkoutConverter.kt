package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout

import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.domain.workout.structure.*

class IntervalsToWorkoutConverter(
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
                ExternalData.empty().withIntervals(eventDTO.id.toString())
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
    ): WorkoutMultiStep {
        return WorkoutMultiStep(
            stepDTO.text,
            stepDTO.reps!!,
            stepDTO.steps!!.map { mapSingleStep(it) }
        )
    }

    private fun mapSingleStep(
        stepDTO: IntervalsWorkoutDocDTO.WorkoutStepDTO
    ): WorkoutSingleStep {
        val targetMapper = IntervalsToTargetConverter(
            workoutDoc!!.ftp?.toDouble(),
            workoutDoc!!.lthr?.toDouble(),
            workoutDoc!!.threshold_pace?.toDouble()
        )
        val mainTarget = targetMapper.toMainTarget(stepDTO)
        val cadenceTarget = stepDTO.cadence?.let { targetMapper.toCadenceTarget(it) }

        return WorkoutSingleStep(
            stepDTO.text,
            StepLength.seconds(stepDTO.duration ?: 600),
            mainTarget,
            cadenceTarget,
            stepDTO.ramp == true
        )
    }
}

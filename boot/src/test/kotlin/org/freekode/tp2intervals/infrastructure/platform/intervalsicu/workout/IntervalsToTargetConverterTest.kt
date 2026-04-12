package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout

import org.freekode.tp2intervals.domain.workout.structure.StepTarget
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class IntervalsToTargetConverterTest {

    @Test
    fun `should convert power target using FTP`() {
        val converter = IntervalsToTargetConverter(ftp = 250.0, lthr = null, paceThreshold = null)
        val stepDTO = IntervalsWorkoutDocDTO.WorkoutStepDTO(
            text = null, reps = null, duration = null, distance = null,
            power = null, _power = IntervalsWorkoutDocDTO.ResolvedStepValueDTO(null, 0.5, 0.75),
            hr = null, _hr = null, pace = null, _pace = null, cadence = null, steps = null,
            warmup = null, cooldown = null, ramp = null
        )

        val result = converter.toMainTarget(stepDTO)

        assertEquals(StepTarget(125, 188), result)
    }

    @Test
    fun `should convert HR target using LTHR`() {
        val converter = IntervalsToTargetConverter(ftp = null, lthr = 150.0, paceThreshold = null)
        val stepDTO = IntervalsWorkoutDocDTO.WorkoutStepDTO(
            text = null, reps = null, duration = null, distance = null,
            power = null, _power = null,
            hr = null, _hr = IntervalsWorkoutDocDTO.ResolvedStepValueDTO(null, 0.7, 0.9),
            pace = null, _pace = null, cadence = null, steps = null,
            warmup = null, cooldown = null, ramp = null
        )

        val result = converter.toMainTarget(stepDTO)

        assertEquals(StepTarget(105, 135), result)
    }

    @Test
    fun `should convert pace target using threshold`() {
        val converter = IntervalsToTargetConverter(ftp = null, lthr = null, paceThreshold = 300.0)
        val stepDTO = IntervalsWorkoutDocDTO.WorkoutStepDTO(
            text = null, reps = null, duration = null, distance = null,
            power = null, _power = null, hr = null, _hr = null,
            pace = null, _pace = IntervalsWorkoutDocDTO.ResolvedStepValueDTO(null, 0.8, 1.0),
            cadence = null, steps = null,
            warmup = null, cooldown = null, ramp = null
        )

        val result = converter.toMainTarget(stepDTO)

        assertEquals(StepTarget(240, 300), result)
    }

    @Test
    fun `should throw when no target available`() {
        val converter = IntervalsToTargetConverter(ftp = null, lthr = null, paceThreshold = null)
        val stepDTO = IntervalsWorkoutDocDTO.WorkoutStepDTO(
            text = null, reps = null, duration = null, distance = null,
            power = null, _power = null, hr = null, _hr = null,
            pace = null, _pace = null, cadence = null, steps = null,
            warmup = null, cooldown = null, ramp = null
        )

        assertThrows<org.freekode.tp2intervals.infrastructure.PlatformException> {
            converter.toMainTarget(stepDTO)
        }
    }

    @Test
    fun `should convert cadence with single value`() {
        val converter = IntervalsToTargetConverter(ftp = 250.0, lthr = null, paceThreshold = null)
        val cadenceDTO = IntervalsWorkoutDocDTO.StepValueDTO(
            units = "rpm",
            value = 90,
            start = null,
            end = null
        )

        val result = converter.toCadenceTarget(cadenceDTO)

        assertEquals(StepTarget(90, 90), result)
    }

    @Test
    fun `should convert cadence with range`() {
        val converter = IntervalsToTargetConverter(ftp = 250.0, lthr = null, paceThreshold = null)
        val cadenceDTO = IntervalsWorkoutDocDTO.StepValueDTO(
            units = "rpm",
            value = null,
            start = 85,
            end = 95
        )

        val result = converter.toCadenceTarget(cadenceDTO)

        assertEquals(StepTarget(85, 95), result)
    }

    @Test
    fun `should prioritize power over HR and pace`() {
        val converter = IntervalsToTargetConverter(ftp = 200.0, lthr = 150.0, paceThreshold = 300.0)
        val stepDTO = IntervalsWorkoutDocDTO.WorkoutStepDTO(
            text = null, reps = null, duration = null, distance = null,
            power = null, _power = IntervalsWorkoutDocDTO.ResolvedStepValueDTO(null, 0.5, 0.5),
            hr = null, _hr = IntervalsWorkoutDocDTO.ResolvedStepValueDTO(null, 0.7, 0.9),
            pace = null, _pace = IntervalsWorkoutDocDTO.ResolvedStepValueDTO(null, 0.8, 1.0),
            cadence = null, steps = null,
            warmup = null, cooldown = null, ramp = null
        )

        val result = converter.toMainTarget(stepDTO)

        assertEquals(StepTarget(100, 100), result)
    }

    @Test
    fun `should use HR when power not available`() {
        val converter = IntervalsToTargetConverter(ftp = null, lthr = 160.0, paceThreshold = null)
        val stepDTO = IntervalsWorkoutDocDTO.WorkoutStepDTO(
            text = null, reps = null, duration = null, distance = null,
            power = null, _power = null,
            hr = null, _hr = IntervalsWorkoutDocDTO.ResolvedStepValueDTO(null, 0.8, 0.9),
            pace = null, _pace = null, cadence = null, steps = null,
            warmup = null, cooldown = null, ramp = null
        )

        val result = converter.toMainTarget(stepDTO)

        assertEquals(StepTarget(128, 144), result)
    }
}

package org.freekode.tp2intervals.domain.workout.structure

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class WorkoutStructureTest {

    @Test
    fun `should throw when steps is empty`() {
        assertThrows<IllegalStateException> {
            WorkoutStructure(WorkoutStructure.TargetUnit.FTP_PERCENTAGE, emptyList())
        }
    }

    @Test
    fun `should create structure with valid steps`() {
        val step = SingleStep(
            "Step 1",
            StepLength.seconds(60),
            StepTarget(50, 100),
            null,
            false
        )

        val structure = WorkoutStructure(WorkoutStructure.TargetUnit.FTP_PERCENTAGE, listOf(step))

        assertEquals(WorkoutStructure.TargetUnit.FTP_PERCENTAGE, structure.target)
        assertEquals(1, structure.steps.size)
    }

    @Test
    fun `should add modifier to structure`() {
        val step = SingleStep(
            "Step 1",
            StepLength.seconds(60),
            StepTarget(50, 100),
            null,
            false
        )
        val structure = WorkoutStructure(WorkoutStructure.TargetUnit.FTP_PERCENTAGE, listOf(step))

        val modified = structure.addModifier(StepModifier.WARMUP)

        assertEquals(StepModifier.WARMUP, modified.modifier)
        assertEquals(structure.steps, modified.steps)
        assertEquals(structure.target, modified.target)
    }

    @Test
    fun `should not modify original when adding modifier`() {
        val step = SingleStep(
            "Step 1",
            StepLength.seconds(60),
            StepTarget(50, 100),
            null,
            false
        )
        val structure = WorkoutStructure(WorkoutStructure.TargetUnit.FTP_PERCENTAGE, listOf(step))

        structure.addModifier(StepModifier.COOLDOWN)

        assertEquals(StepModifier.NONE, structure.modifier)
    }
}

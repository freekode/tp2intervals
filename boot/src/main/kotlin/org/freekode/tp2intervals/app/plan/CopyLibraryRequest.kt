package org.freekode.tp2intervals.app.plan

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainer
import org.freekode.tp2intervals.domain.workout.structure.StepModifier

data class CopyLibraryRequest(
    val libraryContainer: LibraryContainer,
    val newName: String,
    val stepModifier: StepModifier,
    val sourcePlatform: Platform,
    val targetPlatform: Platform,
)

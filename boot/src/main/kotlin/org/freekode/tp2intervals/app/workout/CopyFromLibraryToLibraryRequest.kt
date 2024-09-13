package org.freekode.tp2intervals.app.workout

import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainer
import org.freekode.tp2intervals.rest.workout.WorkoutDetailsDTO

class CopyFromLibraryToLibraryRequest(
    val workoutExternalData: ExternalData,
    val targetLibraryContainer: LibraryContainer,
    val sourcePlatform: Platform,
    val targetPlatform: Platform,
)

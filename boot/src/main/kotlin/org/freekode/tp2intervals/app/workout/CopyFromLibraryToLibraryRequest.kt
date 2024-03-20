package org.freekode.tp2intervals.app.workout

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainer
import org.freekode.tp2intervals.domain.workout.WorkoutDetails

class CopyFromLibraryToLibraryRequest(
    val workoutDetails: WorkoutDetails,
    val toLibraryContainer: LibraryContainer,
    val sourcePlatform: Platform,
    val targetPlatform: Platform,
)

package org.freekode.tp2intervals.app.workout

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainer
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.rest.workout.WorkoutDetailsDTO

class CopyFromLibraryToLibraryRequest(
    val workoutDetails: WorkoutDetailsDTO,
    val targetLibraryContainer: LibraryContainer,
    val sourcePlatform: Platform,
    val targetPlatform: Platform,
)

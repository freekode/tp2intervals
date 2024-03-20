package org.freekode.tp2intervals.app.plan

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainer

data class CopyLibraryRequest(
    val libraryContainer: LibraryContainer,
    val newName: String,
    val sourcePlatform: Platform,
    val targetPlatform: Platform,
)

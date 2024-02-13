package org.freekode.tp2intervals.app.workout

import java.time.LocalDate
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.infrastructure.intervalsicu.folder.FolderDTO

data class CopyWorkoutsRequest(
    val folderType: FolderDTO.FolderType,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val types: List<TrainingType>,
    val sourcePlatform: Platform,
    val targetPlatform: Platform
)

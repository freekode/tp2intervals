package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout

import java.time.LocalDateTime

class TPNoteResponseDTO(
    var id: Long,
    var noteDate: LocalDateTime,
    var title: String,
    var description: String?,
)

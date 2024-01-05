package org.freekode.tp2intervals.domain.plan

import java.time.LocalDate

data class Folder(
    val id: FolderId,
    val startDate: LocalDate
)

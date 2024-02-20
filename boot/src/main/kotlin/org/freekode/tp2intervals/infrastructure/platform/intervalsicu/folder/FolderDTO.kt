package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.folder

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

class FolderDTO(
    val id: String,
    val type: String,
    val name: String,
    @JsonProperty("start_date_local")
    val startDateLocal: LocalDate?,
)

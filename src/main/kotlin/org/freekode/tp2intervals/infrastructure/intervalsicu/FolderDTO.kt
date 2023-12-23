package org.freekode.tp2intervals.infrastructure.intervalsicu

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

class FolderDTO(
    val id: String,
    val type: FolderType,
    val name: String,
    @JsonProperty("start_date_local")
    val startDateLocal: LocalDate?,
) {
    enum class FolderType {
        PLAN,
        FOLDER
    }
}

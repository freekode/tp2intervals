package org.freekode.tp2intervals.infrastructure.platform.trainerroad

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.time.LocalDateTime

class TrainerRoadCalendarTimelineDTO(
    @JsonProperty("PlannedActivities")
    val plannedActivities: List<PlannedActivityDTO>,
    @JsonProperty("Start")
    val start: LocalDateTime? = null,
    @JsonProperty("End")
    val end: LocalDateTime? = null,
) {
    class PlannedActivityDTO(
        @JsonProperty("Id")
        val id: String,
        @JsonProperty("Title")
        val title: String?,
        @JsonProperty("Date")
        val date: CalendarDateDTO,
        @JsonProperty("WorkoutId")
        val workoutId: Long?,
    )

    class CalendarDateDTO(
        @JsonProperty("Year")
        val year: Int,
        @JsonProperty("Month")
        val month: Int,
        @JsonProperty("Day")
        val day: Int,
    ) {
        fun toLocalDate(): LocalDate = LocalDate.of(year, month, day)
    }
}

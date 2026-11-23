package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Entity
@Table(name = "trainerroad_synced_events")
class TrainerRoadSyncedEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "athlete_id", nullable = false)
    val athleteId: String,
    @Column(name = "event_id", nullable = false)
    val eventId: String,
    @Column(name = "trainer_road_id", nullable = false)
    val trainerRoadId: String,
) {
    constructor() : this(null, "", "", "")
}

@Repository
interface TrainerRoadSyncedEventRepository : CrudRepository<TrainerRoadSyncedEvent, Long> {
    fun findByAthleteIdAndEventId(athleteId: String, eventId: String): TrainerRoadSyncedEvent?

    fun findByAthleteIdAndEventIdAndTrainerRoadId(
        athleteId: String, eventId: String, trainerRoadId: String,
    ): TrainerRoadSyncedEvent?
}

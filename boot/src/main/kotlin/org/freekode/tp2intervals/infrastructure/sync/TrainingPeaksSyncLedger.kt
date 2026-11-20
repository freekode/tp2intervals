package org.freekode.tp2intervals.infrastructure.sync

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class TrainingPeaksSyncLedger(private val jdbc: JdbcTemplate) {
    data class Key(val sourceAccountId: String, val destinationAthleteId: String, val sourceWorkoutId: String)

    enum class State { PENDING, CONFIRMED, AMBIGUOUS }

    data class Entry(val state: State, val remoteEventId: Long?)

    fun find(key: Key): Entry? = jdbc.query(
        """
        SELECT state, remote_event_id FROM trainingpeaks_sync_ledger
        WHERE source_account_id = ? AND destination_athlete_id = ? AND source_workout_id = ?
        """.trimIndent(),
        { row, _ -> Entry(State.valueOf(row.getString("state")), row.getObject("remote_event_id")?.let { row.getLong("remote_event_id") }) },
        key.sourceAccountId, key.destinationAthleteId, key.sourceWorkoutId
    ).singleOrNull()

    fun claim(key: Key): Boolean = insert(key, State.PENDING)

    fun recordLegacyMatch(key: Key, remoteEventId: Long): Boolean = insert(key, State.CONFIRMED, remoteEventId)

    fun recordAmbiguousMatch(key: Key): Boolean = insert(key, State.AMBIGUOUS)

    fun confirm(key: Key, remoteEventId: Long) {
        jdbc.update(
            """
            UPDATE trainingpeaks_sync_ledger SET state = 'CONFIRMED', remote_event_id = ?
            WHERE source_account_id = ? AND destination_athlete_id = ? AND source_workout_id = ? AND state = 'PENDING'
            """.trimIndent(),
            remoteEventId, key.sourceAccountId, key.destinationAthleteId, key.sourceWorkoutId
        )
    }

    private fun insert(key: Key, state: State, remoteEventId: Long? = null): Boolean = jdbc.update(
        """
        INSERT INTO trainingpeaks_sync_ledger
          (source_account_id, destination_athlete_id, source_workout_id, state, remote_event_id)
        VALUES (?, ?, ?, ?, ?)
        ON CONFLICT (source_account_id, destination_athlete_id, source_workout_id) DO NOTHING
        """.trimIndent(),
        key.sourceAccountId, key.destinationAthleteId, key.sourceWorkoutId, state.name, remoteEventId
    ) == 1
}

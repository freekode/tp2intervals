package org.freekode.tp2intervals.infrastructure.sync

import org.freekode.tp2intervals.config.PreviewOnly
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class TrainingPeaksSyncLedger(private val jdbc: JdbcTemplate, private val mode: PreviewOnly) {
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

    fun idsForDestination(source: String, destination: String): List<String> = jdbc.query(
        """
        SELECT source_workout_id FROM trainingpeaks_sync_ledger
        WHERE source_account_id = ? AND destination_athlete_id = ?
        """.trimIndent(),
        { row, _ -> row.getString(1) }, source, destination
    )

    fun unresolvedIds(source: String, destination: String): List<String> = jdbc.query(
        """
        SELECT source_workout_id FROM trainingpeaks_sync_ledger
        WHERE source_account_id = ? AND destination_athlete_id = ? AND state IN ('PENDING', 'AMBIGUOUS')
        """.trimIndent(),
        { row, _ -> row.getString(1) }, source, destination
    )

    fun claim(key: Key): Boolean = insert(key, State.PENDING)

    fun claimForSync(key: Key, approval: TrainingPeaksLegacyBaselineRepository.Approval?): Boolean {
        mode.requireWritable()
        return jdbc.update(
            """
            INSERT INTO trainingpeaks_sync_ledger
              (source_account_id, destination_athlete_id, source_workout_id, state)
            SELECT ?, ?, ?, 'PENDING'
            WHERE NOT EXISTS (
              SELECT 1 FROM trainingpeaks_legacy_hold
              WHERE source_account_id = ? AND destination_athlete_id = ? AND source_workout_id = ?
            )
            AND (? IS NULL OR EXISTS (
              SELECT 1 FROM trainingpeaks_legacy_baseline b JOIN trainingpeaks_sync_generation g
              ON b.generation = g.generation
              WHERE b.id = ? AND b.state = 'APPROVED' AND b.source_account_id = ? AND b.destination_athlete_id = ?
            ))
            ON CONFLICT (source_account_id, destination_athlete_id, source_workout_id) DO NOTHING
            """.trimIndent(),
            key.sourceAccountId, key.destinationAthleteId, key.sourceWorkoutId,
            key.sourceAccountId, key.destinationAthleteId, key.sourceWorkoutId,
            approval?.id, approval?.id, key.sourceAccountId, key.destinationAthleteId,
        ) == 1
    }

    fun claimReviewedUnmatchedHold(
        key: Key, approval: TrainingPeaksLegacyBaselineRepository.Approval, inventoryVersion: Long,
    ): Boolean {
        mode.requireWritable()
        return jdbc.update(
            """
            INSERT INTO trainingpeaks_sync_ledger
              (source_account_id, destination_athlete_id, source_workout_id, state)
            SELECT h.source_account_id, h.destination_athlete_id, h.source_workout_id, 'PENDING'
            FROM trainingpeaks_legacy_hold h
            JOIN trainingpeaks_legacy_baseline b
              ON b.source_account_id = h.source_account_id AND b.destination_athlete_id = h.destination_athlete_id
            JOIN trainingpeaks_sync_generation g ON b.generation = g.generation
            WHERE h.source_account_id = ? AND h.destination_athlete_id = ? AND h.source_workout_id = ?
              AND h.reason = 'LISTINGS_INCONCLUSIVE'
              AND b.id = ? AND b.state = 'APPROVED'
              AND b.id = (SELECT MAX(id) FROM trainingpeaks_legacy_baseline)
              AND g.inventory_version = ?
            ON CONFLICT (source_account_id, destination_athlete_id, source_workout_id) DO NOTHING
            """.trimIndent(),
            key.sourceAccountId, key.destinationAthleteId, key.sourceWorkoutId, approval.id, inventoryVersion
        ) == 1
    }

    fun recordLegacyMatch(key: Key, remoteEventId: Long): Boolean = insert(key, State.CONFIRMED, remoteEventId)

    fun recordAmbiguousMatch(key: Key): Boolean = insert(key, State.AMBIGUOUS)

    fun confirm(key: Key, remoteEventId: Long) {
        mode.requireWritable()
        jdbc.update(
            """
            UPDATE trainingpeaks_sync_ledger SET state = 'CONFIRMED', remote_event_id = ?
            WHERE source_account_id = ? AND destination_athlete_id = ? AND source_workout_id = ? AND state = 'PENDING'
            """.trimIndent(),
            remoteEventId, key.sourceAccountId, key.destinationAthleteId, key.sourceWorkoutId
        )
    }

    private fun insert(key: Key, state: State, remoteEventId: Long? = null): Boolean {
        mode.requireWritable()
        return jdbc.update(
            """
            INSERT INTO trainingpeaks_sync_ledger
              (source_account_id, destination_athlete_id, source_workout_id, state, remote_event_id)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (source_account_id, destination_athlete_id, source_workout_id) DO NOTHING
            """.trimIndent(),
            key.sourceAccountId, key.destinationAthleteId, key.sourceWorkoutId, state.name, remoteEventId
        ) == 1
    }
}

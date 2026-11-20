package org.freekode.tp2intervals.infrastructure.sync

import org.freekode.tp2intervals.config.PreviewOnly
import org.freekode.tp2intervals.infrastructure.sync.TrainingPeaksSyncLedger.Key
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.jdbc.support.JdbcTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDate

@Repository
class TrainingPeaksLegacyBaselineRepository(
    private val jdbc: JdbcTemplate,
    private val ledger: TrainingPeaksSyncLedger,
    private val mode: PreviewOnly,
) {
    private val transaction = TransactionTemplate(JdbcTransactionManager(checkNotNull(jdbc.dataSource)))

    data class Accounts(val source: String, val destination: String, val generation: Long)
    data class Approval(val id: Long, val accounts: Accounts, val state: String, val revision: String, val approvedAt: String)

    fun generation(): Long = checkNotNull(
        jdbc.queryForObject("SELECT generation FROM trainingpeaks_sync_generation WHERE singleton = 1", Long::class.java)
    )

    fun inventoryVersion(): Long = checkNotNull(
        jdbc.queryForObject("SELECT inventory_version FROM trainingpeaks_sync_generation WHERE singleton = 1", Long::class.java)
    )

    fun latest(): Approval? = jdbc.query(
        "SELECT * FROM trainingpeaks_legacy_baseline ORDER BY id DESC LIMIT 1"
    ) { row, _ ->
        Approval(
            row.getLong("id"), Accounts(row.getString("source_account_id"), row.getString("destination_athlete_id"), row.getLong("generation")),
            row.getString("state"), row.getString("revision"), row.getString("approved_at")
        )
    }.singleOrNull()

    fun usableApproval(accounts: Accounts): Approval? {
        val latest = latest()?.takeIf { it.state == "APPROVED" } ?: return null
        if (latest.accounts == accounts) return latest
        mode.requireWritable()
        jdbc.update(
            """
            UPDATE trainingpeaks_legacy_baseline SET state = 'INVALIDATED'
            WHERE id = ? AND state = 'APPROVED'
              AND ? = (SELECT generation FROM trainingpeaks_sync_generation WHERE singleton = 1)
            """.trimIndent(),
            latest.id, accounts.generation,
        )
        return null
    }

    fun isCurrent(approval: Approval): Boolean =
        latest() == approval && generation() == approval.accounts.generation

    fun isHeld(key: Key): Boolean = jdbc.queryForObject(
        """
        SELECT EXISTS(SELECT 1 FROM trainingpeaks_legacy_hold
          WHERE source_account_id = ? AND destination_athlete_id = ? AND source_workout_id = ?)
        """.trimIndent(), Int::class.java, key.sourceAccountId, key.destinationAthleteId, key.sourceWorkoutId
    ) == 1

    fun approve(
        accounts: Accounts,
        previousId: Long,
        inventoryVersion: Long,
        revision: String,
        start: LocalDate,
        end: LocalDate,
        reviewJson: String,
        links: Map<String, Long>,
        holds: Map<String, String>,
    ): Approval? {
        mode.requireWritable()
        return transaction.execute {
            persist(accounts, previousId, inventoryVersion, revision, start, end, reviewJson, links, holds)
        }
    }

    private fun persist(
        accounts: Accounts, previousId: Long, inventoryVersion: Long, revision: String,
        start: LocalDate, end: LocalDate, reviewJson: String, links: Map<String, Long>, holds: Map<String, String>,
    ): Approval? {
        val inserted = jdbc.update(
            """
            INSERT INTO trainingpeaks_legacy_baseline
                (source_account_id, destination_athlete_id, generation, revision, start_date, end_date, review_json, state)
            SELECT ?, ?, ?, ?, ?, ?, ?, 'APPROVED'
            WHERE ? = (SELECT generation FROM trainingpeaks_sync_generation WHERE singleton = 1)
              AND ? = COALESCE((SELECT MAX(id) FROM trainingpeaks_legacy_baseline), 0)
              AND ? = (SELECT inventory_version FROM trainingpeaks_sync_generation WHERE singleton = 1)
            """.trimIndent(),
            accounts.source, accounts.destination, accounts.generation, revision, start.toString(), end.toString(),
            reviewJson, accounts.generation, previousId, inventoryVersion,
        )
        if (inserted != 1) return null
        val approval = checkNotNull(latest())
        jdbc.update("UPDATE trainingpeaks_legacy_baseline SET state = 'SUPERSEDED' WHERE state = 'APPROVED' AND id <> ?", approval.id)
        links.forEach { (id, event) ->
            ledger.recordLegacyMatch(Key(accounts.source, accounts.destination, id), event)
        }
        holds.forEach { (id, reason) ->
            jdbc.update(
                """
                INSERT INTO trainingpeaks_legacy_hold
                  (source_account_id, destination_athlete_id, source_workout_id, baseline_id, reason)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (source_account_id, destination_athlete_id, source_workout_id) DO NOTHING
                """.trimIndent(),
                accounts.source, accounts.destination, id, approval.id, reason,
            )
        }
        return approval
    }
}

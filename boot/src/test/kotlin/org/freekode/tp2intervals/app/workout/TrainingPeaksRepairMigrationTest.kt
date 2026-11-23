package org.freekode.tp2intervals.app.workout

import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.integration.spring.SpringResourceAccessor
import org.springframework.core.io.DefaultResourceLoader
import org.freekode.tp2intervals.config.PreviewOnly
import org.freekode.tp2intervals.infrastructure.sync.TrainingPeaksLegacyBaselineRepository
import org.freekode.tp2intervals.infrastructure.sync.TrainingPeaksSyncLedger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.jdbc.core.JdbcTemplate
import org.sqlite.SQLiteDataSource
import java.nio.file.Path
import java.time.LocalDate

class TrainingPeaksRepairMigrationTest {
    @TempDir
    lateinit var temp: Path

    @Test
    fun `migration 0011 preserves a deployed baseline and confirmed link and records the repair`() {
        val dataSource = SQLiteDataSource().apply { url = "jdbc:sqlite:${temp.resolve("upgrade.sqlite")}" }
        fun migrate(limit: Int?) {
            val database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(JdbcConnection(dataSource.connection))
            Liquibase("db/changelog/db.changelog-master.yaml", SpringResourceAccessor(DefaultResourceLoader()), database).use {
                if (limit == null) it.update(Contexts(), LabelExpression())
                else it.update(limit, Contexts(), LabelExpression())
            }
        }
        migrate(11) // Existing changesets through the deployed 0010 schema.
        val jdbc = JdbcTemplate(dataSource)
        val mode = PreviewOnly(false)
        val ledger = TrainingPeaksSyncLedger(jdbc, mode)
        val baselines = TrainingPeaksLegacyBaselineRepository(jdbc, ledger, mode)
        val accounts = TrainingPeaksLegacyBaselineRepository.Accounts("synthetic", "iSynthetic", baselines.generation())
        val key = TrainingPeaksSyncLedger.Key(accounts.source, accounts.destination, "workout")
        val day = LocalDate.of(2026, 11, 23)
        val approval = checkNotNull(baselines.approve(
            accounts, 0, baselines.inventoryVersion(), "baseline", day, day, "{}", mapOf("workout" to 10L), emptyMap()
        ))
        migrate(null)
        assertEquals(approval, baselines.latest())
        assertEquals(10L, ledger.find(key)?.remoteEventId)
        assertTrue(ledger.claimLinkRepair(key, approval, baselines.inventoryVersion(), "review", 10, null))
        assertFalse(ledger.confirmLinkRepair(key, 10))
        assertTrue(ledger.confirmLinkRepair(key, 11))
        val audit = jdbc.queryForMap("SELECT * FROM trainingpeaks_link_repair")
        assertEquals(10L, (audit["old_event_id"] as Number).toLong())
        assertEquals(11L, (audit["new_event_id"] as Number).toLong())
        assertEquals("review", audit["reviewed_revision"])
        assertEquals(11L, ledger.find(key)?.remoteEventId)
    }
}

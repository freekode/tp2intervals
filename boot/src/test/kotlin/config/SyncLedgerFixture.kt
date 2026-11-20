package config

import liquibase.integration.spring.SpringLiquibase
import org.freekode.tp2intervals.config.PreviewOnly
import org.freekode.tp2intervals.infrastructure.sync.TrainingPeaksLegacyBaselineRepository
import org.freekode.tp2intervals.infrastructure.sync.TrainingPeaksLegacyBaselineRepository.Accounts
import org.freekode.tp2intervals.infrastructure.sync.TrainingPeaksSyncLedger
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.jdbc.core.JdbcTemplate
import org.sqlite.SQLiteDataSource
import java.nio.file.Path
import java.time.LocalDate

class SyncLedgerFixture(private val path: Path) {
    init {
        SpringLiquibase().apply {
            dataSource = newDataSource()
            resourceLoader = DefaultResourceLoader()
            changeLog = "classpath:db/changelog/db.changelog-master.yaml"
            afterPropertiesSet()
        }
    }

    fun newLedger(readOnly: Boolean = false, mode: PreviewOnly = PreviewOnly(false)) =
        TrainingPeaksSyncLedger(jdbc(readOnly), mode)

    fun baselines(readOnly: Boolean = false, mode: PreviewOnly = PreviewOnly(false)): TrainingPeaksLegacyBaselineRepository {
        val connection = jdbc(readOnly)
        return TrainingPeaksLegacyBaselineRepository(connection, TrainingPeaksSyncLedger(connection, mode), mode)
    }

    fun seedBaseline(source: String, destination: String) {
        val baseline = baselines()
        if (baseline.latest() != null) return
        val date = LocalDate.of(2025, 2, 12)
        checkNotNull(baseline.approve(
            Accounts(source, destination, baseline.generation()), 0, baseline.inventoryVersion(), "test-review",
            date, date, "{}", emptyMap(), emptyMap()
        ))
    }

    fun jdbc(readOnly: Boolean = false) = JdbcTemplate(newDataSource().apply { setReadOnly(readOnly) })

    private fun newDataSource() = SQLiteDataSource().apply {
        url = "jdbc:sqlite:$path"
    }
}

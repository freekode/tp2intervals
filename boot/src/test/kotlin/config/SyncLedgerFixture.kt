package config

import liquibase.integration.spring.SpringLiquibase
import org.freekode.tp2intervals.infrastructure.sync.TrainingPeaksSyncLedger
import org.springframework.jdbc.core.JdbcTemplate
import org.sqlite.SQLiteDataSource
import java.nio.file.Path

class SyncLedgerFixture(private val path: Path) {
    init {
        SpringLiquibase().apply {
            dataSource = newDataSource()
            changeLog = "classpath:db/changelog/schemas/0009-add-trainingpeaks-sync-ledger.yaml"
            afterPropertiesSet()
        }
    }

    fun newLedger(readOnly: Boolean = false) =
        TrainingPeaksSyncLedger(JdbcTemplate(newDataSource().apply { setReadOnly(readOnly) }))

    private fun newDataSource() = SQLiteDataSource().apply {
        url = "jdbc:sqlite:$path"
    }
}

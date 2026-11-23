package org.freekode.tp2intervals.app.workout

import config.SyncLedgerFixture
import org.freekode.tp2intervals.infrastructure.sync.TrainingPeaksSyncLedger.Key
import org.freekode.tp2intervals.infrastructure.sync.TrainingPeaksSyncLedger.State
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class TrainingPeaksSyncLedgerTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `claims are unique per source account destination athlete and workout ID`() {
        val database = SyncLedgerFixture(tempDir.resolve("sync.sqlite"))
        val first = database.newLedger()
        val restarted = database.newLedger()
        val key = Key("source", "destination", "12345")
        assertTrue(first.claim(key))
        assertFalse(restarted.claim(key))
        assertTrue(restarted.claim(key.copy(sourceAccountId = "other source")))
        assertTrue(restarted.claim(key.copy(destinationAthleteId = "other destination")))
        assertTrue(restarted.claim(key.copy(sourceWorkoutId = "67890")))

        first.confirm(key, 123L)
        restarted.confirm(key, 456L)

        val confirmed = restarted.find(key)
        assertEquals(State.CONFIRMED, confirmed?.state)
        assertEquals(123L, confirmed?.remoteEventId)
    }
}

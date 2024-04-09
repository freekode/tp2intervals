package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout

import config.SpringITConfig
import java.time.LocalDate
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainer
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class TrainingPeaksWorkoutRepositoryTest : SpringITConfig() {
    @Autowired
    lateinit var trainingPeaksWorkoutRepository: TrainingPeaksWorkoutRepository

    @Test
    fun `should parse workouts`() {
        val container =
            LibraryContainer("lib", LocalDate.now(), false, 0, ExternalData.empty().withTrainingPeaks("tp-id"))
        val workouts = trainingPeaksWorkoutRepository.getWorkoutsFromLibrary(container)

        assertTrue(workouts.isNotEmpty())
    }
}

package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout

import config.SpringITConfig
import java.time.LocalDate
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class TrainingPeaksWorkoutRepositoryIT : SpringITConfig() {
    @Autowired
    lateinit var trainingPeaksWorkoutRepository: TrainingPeaksWorkoutRepository

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            Thread.sleep(1000)
        }
    }

    @Test
    fun ``() {

    }

//    @Test
//    fun `should parse workout library`() {
//        val container =
//            LibraryContainer("lib", LocalDate.now(), false, 0, ExternalData.empty().withTrainingPeaks("tp-id"))
//        val workouts = trainingPeaksWorkoutRepository.getWorkoutsFromLibrary(container)
//
//        assertEquals(workouts.size, 1)
//    }
//
//    @Test
//    fun `should parse calendar workout`() {
//        val workouts = trainingPeaksWorkoutRepository.getWorkoutsFromCalendar(LocalDate.now(), LocalDate.now())
//
//        assertEquals(workouts.size, 7)
//    }
}

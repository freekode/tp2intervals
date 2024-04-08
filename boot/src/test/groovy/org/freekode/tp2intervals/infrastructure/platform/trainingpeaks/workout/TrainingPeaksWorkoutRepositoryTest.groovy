package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout

import org.freekode.tp2intervals.config.SpringIT
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainer
import org.springframework.beans.factory.annotation.Autowired

import java.time.LocalDate

class TrainingPeaksWorkoutRepositoryTest extends SpringIT {
    @Autowired
    TrainingPeaksWorkoutRepository trainingPeaksWorkoutRepository

    def "should"() {
        def container = new LibraryContainer("lib", LocalDate.now(), false, 0, new ExternalData("123", null, null,))
        def workouts = trainingPeaksWorkoutRepository.getWorkoutsFromLibrary(container)

        expect:
        workouts.size() > 0
    }
}

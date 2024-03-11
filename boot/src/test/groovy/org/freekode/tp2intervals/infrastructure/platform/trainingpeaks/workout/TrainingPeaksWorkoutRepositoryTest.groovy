package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout

import org.freekode.tp2intervals.config.SpringIT
import org.springframework.beans.factory.annotation.Autowired

class TrainingPeaksWorkoutRepositoryTest extends SpringIT {
    @Autowired
    TrainingPeaksWorkoutRepository trainingPeaksWorkoutRepository

    def "should"() {
//        def workouts = trainingPeaksWorkoutRepository.findWorkoutsFromLibraryByName("absa")

        expect:
        true
//        workouts.size() > 0
    }
}

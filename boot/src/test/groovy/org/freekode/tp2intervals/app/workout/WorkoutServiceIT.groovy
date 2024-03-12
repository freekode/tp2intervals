package org.freekode.tp2intervals.app.workout


import org.freekode.tp2intervals.config.SpringIT
import org.freekode.tp2intervals.domain.Platform
import org.springframework.beans.factory.annotation.Autowired

class WorkoutServiceIT extends SpringIT {
    @Autowired
    WorkoutService workoutService

    def "should"() {
        def workouts = workoutService.findWorkoutsByName(Platform.TRAINER_ROAD, "abney")

        expect:
        workouts.size() > 0
    }
}

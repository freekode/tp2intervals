package org.freekode.tp2intervals.infrastructure.platform.trainerroad.activity

import org.freekode.tp2intervals.config.ISpringConfiguration
import org.freekode.tp2intervals.domain.config.AppConfigurationRepository
import org.freekode.tp2intervals.domain.config.UpdateConfigurationRequest
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.activity.TrainerRoadActivityRepository
import org.springframework.beans.factory.annotation.Autowired

import java.time.LocalDate

class TrainerRoadActivityRepositoryIT extends ISpringConfiguration {
    @Autowired
    TrainerRoadActivityRepository trainerRoadActivityRepository

    @Autowired
    AppConfigurationRepository appConfigurationRepository

    def setup() {
        appConfigurationRepository.updateConfig(new UpdateConfigurationRequest(
                Map.of(
                        "trainer-road.auth-cookie", "my-cookie",
                )
        ))
    }

    def "should parse and map activities"() {
        when:
        def activities = trainerRoadActivityRepository.getActivities(LocalDate.now(), LocalDate.now())

        then:
        activities != null
    }

}

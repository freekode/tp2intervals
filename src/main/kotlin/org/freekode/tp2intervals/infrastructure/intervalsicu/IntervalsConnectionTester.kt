package org.freekode.tp2intervals.infrastructure.intervalsicu

import org.freekode.tp2intervals.app.ConnectionTester
import org.freekode.tp2intervals.infrastructure.config.ConfigurationRepository
import org.springframework.stereotype.Component

@Component
class IntervalsConnectionTester(
    private val intervalsApiClient: IntervalsApiClient,
    private val configurationRepository: ConfigurationRepository,
) : ConnectionTester {
    override fun test() {
        intervalsApiClient.getAthlete(configurationRepository.getConfiguration().intervalsAthleteId)
    }
}

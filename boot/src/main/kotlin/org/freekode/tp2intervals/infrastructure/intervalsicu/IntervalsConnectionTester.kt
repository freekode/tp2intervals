package org.freekode.tp2intervals.infrastructure.intervalsicu

import org.freekode.tp2intervals.app.ConnectionTester
import org.freekode.tp2intervals.app.Platform
import org.freekode.tp2intervals.domain.config.AppConfigRepository
import org.springframework.stereotype.Component

@Component
class IntervalsConnectionTester(
    private val intervalsApiClient: IntervalsApiClient,
    private val appConfigRepository: AppConfigRepository,
) : ConnectionTester {
    override fun platform() = Platform.INTERVALS

    override fun test() {
        intervalsApiClient.getAthlete(appConfigRepository.getConfig().intervalsConfig.athleteId)
    }
}

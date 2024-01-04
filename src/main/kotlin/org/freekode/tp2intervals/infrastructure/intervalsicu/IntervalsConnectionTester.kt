package org.freekode.tp2intervals.infrastructure.intervalsicu

import org.freekode.tp2intervals.app.ConnectionTester
import org.springframework.stereotype.Component

@Component
class IntervalsConnectionTester(
    private val intervalsApiClient: IntervalsApiClient,
    private val intervalsProperties: IntervalsProperties,
) : ConnectionTester {
    override fun test() {
        intervalsApiClient.getAthlete(intervalsProperties.athleteId)
    }
}

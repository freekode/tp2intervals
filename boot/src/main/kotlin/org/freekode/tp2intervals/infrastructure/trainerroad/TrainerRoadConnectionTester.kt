package org.freekode.tp2intervals.infrastructure.trainerroad

import org.freekode.tp2intervals.app.ConnectionTester
import org.freekode.tp2intervals.app.Platform
import org.springframework.stereotype.Component

@Component
class TrainerRoadConnectionTester(
    private val trainerRoadApiClient: TrainerRoadApiClient
) : ConnectionTester {
    override fun platform() = Platform.TRAINER_ROAD

    override fun test() {
        trainerRoadApiClient.getMember()
    }
}

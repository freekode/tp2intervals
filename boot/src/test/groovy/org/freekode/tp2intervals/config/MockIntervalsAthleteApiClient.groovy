package org.freekode.tp2intervals.config

import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsAthleteApiClient

class MockIntervalsAthleteApiClient implements IntervalsAthleteApiClient {
    @Override
    Map<String, Object> getAthlete(String athleteId, String authorization) {
        return Map.of("test", "passed")
    }
}

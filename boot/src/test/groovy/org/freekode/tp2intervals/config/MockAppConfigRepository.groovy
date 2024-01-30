package org.freekode.tp2intervals.config


import org.freekode.tp2intervals.domain.config.AppConfig
import org.freekode.tp2intervals.domain.config.AppConfigRepository
import org.freekode.tp2intervals.domain.config.IntervalsConfig
import org.freekode.tp2intervals.domain.config.TrainingPeaksConfig
import org.jetbrains.annotations.NotNull

class MockAppConfigRepository implements AppConfigRepository {
    AppConfig config = new AppConfig(new TrainingPeaksConfig("tp auth"), new IntervalsConfig("api", "athlete"))

    @Override
    AppConfig getConfig() {
        return config
    }

    @Override
    AppConfig findConfig() {
        return config
    }

    @Override
    AppConfig updateConfig(@NotNull AppConfig appConfig) {
        return config
    }
}

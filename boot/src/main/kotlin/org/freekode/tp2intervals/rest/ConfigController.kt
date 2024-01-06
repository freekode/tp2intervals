package org.freekode.tp2intervals.rest

import org.freekode.tp2intervals.app.ConfigService
import org.freekode.tp2intervals.app.ConnectionTester
import org.freekode.tp2intervals.domain.config.AppConfig
import org.freekode.tp2intervals.domain.config.IntervalsConfig
import org.freekode.tp2intervals.domain.config.TrainingPeaksConfig
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ConfigController(
    private val configService: ConfigService,
) {

    @GetMapping("/api/test-connections")
    fun testConnections() = configService.testConnections()

    @GetMapping("/api/config")
    fun getConfig(): AppConfigDTO? {
        val config = configService.findConfig() ?: return null
        return AppConfigDTO(
            config.tpConfig.authCookie,
            config.intervalsConfig.apiKey,
            config.intervalsConfig.athleteId
        )
    }

    @PutMapping("/api/config")
    fun updateConfig(@RequestBody appConfigDTO: AppConfigDTO) {
        val appConfig = AppConfig(
            TrainingPeaksConfig(appConfigDTO.tpAuthCookie),
            IntervalsConfig(appConfigDTO.intervalsApiKey, appConfigDTO.intervalsAthleteId)
        )
        configService.updateConfig(appConfig)
        val errors = configService.testConnections()
        if (errors.isNotEmpty()) {
            throw IllegalStateException(errors.joinToString())
        }
    }

}

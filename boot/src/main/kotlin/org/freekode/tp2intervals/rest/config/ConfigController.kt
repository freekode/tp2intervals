package org.freekode.tp2intervals.rest.config

import org.freekode.tp2intervals.app.ConfigService
import org.freekode.tp2intervals.domain.config.AppConfig
import org.freekode.tp2intervals.domain.config.IntervalsConfig
import org.freekode.tp2intervals.domain.config.TrainerRoadConfig
import org.freekode.tp2intervals.domain.config.TrainingPeaksConfig
import org.freekode.tp2intervals.rest.ErrorResponseDTO
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
            config.trConfig.authCookie,
            config.intervalsConfig.apiKey,
            config.intervalsConfig.athleteId
        )
    }

    @PutMapping("/api/config")
    fun updateConfig(@RequestBody appConfigDTO: AppConfigDTO): ResponseEntity<ErrorResponseDTO> {
        val appConfig = AppConfig(
            TrainingPeaksConfig(appConfigDTO.tpAuthCookie),
            TrainerRoadConfig(appConfigDTO.trAuthCookie),
            IntervalsConfig(appConfigDTO.intervalsApiKey, appConfigDTO.intervalsAthleteId)
        )
        configService.updateConfig(appConfig)
        val errors = configService.testConnections()
        if (errors.isNotEmpty()) {
            return ResponseEntity.badRequest().body(ErrorResponseDTO(errors.joinToString()))
        }
        return ResponseEntity.ok().build()
    }

}

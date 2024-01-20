package org.freekode.tp2intervals.rest.config

import org.freekode.tp2intervals.app.config.ConfigService
import org.freekode.tp2intervals.app.Platform
import org.freekode.tp2intervals.domain.config.AppConfig
import org.freekode.tp2intervals.domain.config.IntervalsConfig
import org.freekode.tp2intervals.domain.config.TrainerRoadConfig
import org.freekode.tp2intervals.domain.config.TrainingPeaksConfig
import org.freekode.tp2intervals.rest.ErrorResponseDTO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ConfigController(
    private val configService: ConfigService,
) {

    @GetMapping("/api/config")
    fun getConfig(): AppConfigDTO? {
        val config = configService.findConfig() ?: return null
        return AppConfigDTO(
            config.tpConfig?.authCookie,
            config.trConfig?.authCookie,
            config.intervalsConfig.apiKey,
            config.intervalsConfig.athleteId
        )
    }

    @PutMapping("/api/config")
    fun updateConfig(@RequestBody appConfigDTO: AppConfigDTO): ResponseEntity<ErrorResponseDTO> {
        val appConfig = toDomain(appConfigDTO)
        val errors = configService.validateAllConfiguration(appConfig)
        configService.updateConfig(appConfig)
        if (errors.isNotEmpty()) {
            return ResponseEntity.badRequest().body(ErrorResponseDTO(errors.joinToString()))
        }
        return ResponseEntity.ok().build()
    }

    @GetMapping("/api/config/validate")
    fun validateAllConfiguration(
        @RequestBody appConfigDTO: AppConfigDTO
    ): ResponseEntity<ErrorResponseDTO> {
        val appConfig = toDomain(appConfigDTO)
        val errors = configService.validateAllConfiguration(appConfig)
        if (errors.isNotEmpty()) {
            return ResponseEntity.badRequest().body(ErrorResponseDTO(errors.joinToString()))
        }
        return ResponseEntity.ok().build()
    }

    @GetMapping("/api/config/validate/{platform}")
    fun validateConfiguration(
        @PathVariable platform: Platform,
        @RequestBody appConfigDTO: AppConfigDTO
    ): ResponseEntity<ErrorResponseDTO> {
        val appConfig = toDomain(appConfigDTO)
        val error = configService.validateConfiguration(platform, appConfig)
        if (error != null) {
            return ResponseEntity.badRequest().body(ErrorResponseDTO(error))
        }
        return ResponseEntity.ok().build()
    }


    private fun toDomain(appConfigDTO: AppConfigDTO): AppConfig {
        return AppConfig(
            appConfigDTO.tpAuthCookie?.let { TrainingPeaksConfig(it) },
            appConfigDTO.trAuthCookie?.let { TrainerRoadConfig(it) },
            IntervalsConfig(appConfigDTO.intervalsApiKey, appConfigDTO.intervalsAthleteId)
        )
    }

}

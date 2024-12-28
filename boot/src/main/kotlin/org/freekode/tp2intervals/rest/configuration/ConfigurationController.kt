package org.freekode.tp2intervals.rest.configuration

import org.freekode.tp2intervals.app.confguration.ConfigurationService
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.config.PlatformInfo
import org.freekode.tp2intervals.domain.config.UpdateConfigurationRequest
import org.freekode.tp2intervals.domain.workout.structure.StepModifier
import org.freekode.tp2intervals.rest.ErrorResponseDTO
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ConfigurationController(
    private val configurationService: ConfigurationService,
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @GetMapping("/api/configuration")
    fun getConfigurations(): AppConfigurationDTO {
        log.info("Received request for getting all configurations")
        val configurations = configurationService.getConfigurations()
        return AppConfigurationDTO(configurations.configMap)
    }

    @PutMapping("/api/configuration")
    fun updateConfiguration(@RequestBody requestDTO: UpdateConfigurationRequestDTO): ResponseEntity<ErrorResponseDTO> {
        log.info("Received request for updating configuration: $requestDTO")
        val errors = configurationService.updateConfiguration(UpdateConfigurationRequest(requestDTO.config))
        if (errors.isNotEmpty()) {
            return ResponseEntity.badRequest().body(ErrorResponseDTO(errors.joinToString()))
        }
        return ResponseEntity.ok().build()
    }

    @Deprecated("all config on ui")
    @GetMapping("/api/configuration/training-types")
    fun getTrainingTypes(): List<TrainingTypeDTO> {
        return TrainingType.entries.map { TrainingTypeDTO(it) }
    }

    @GetMapping("/api/configuration/intervals-step-modifiers")
    fun getIntervalsStepModifiers(): List<StepModifier> {
        return StepModifier.entries
    }

    @GetMapping("/api/configuration/{platform}")
    fun getConfigurations(@PathVariable platform: Platform): PlatformInfo {
        log.info("Received request for getting configurations for platform: $platform")
        return configurationService.platformInfo(platform)
    }
}

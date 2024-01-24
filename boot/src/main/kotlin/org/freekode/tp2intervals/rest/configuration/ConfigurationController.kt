package org.freekode.tp2intervals.rest.configuration

import org.freekode.tp2intervals.app.confguration.ConfigurationService
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.config.AppConfiguration
import org.freekode.tp2intervals.domain.config.UpdateConfigurationRequest
import org.freekode.tp2intervals.rest.ErrorResponseDTO
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

    @GetMapping("/api/configuration")
    fun getConfigurations(): AppConfigurationDTO? {
        val configurations = configurationService.getConfigurations()
        return AppConfigurationDTO(configurations.configMap)
    }

    @PutMapping("/api/configuration")
    fun updateConfiguration(@RequestBody requestDTO: UpdateConfigurationRequestDTO): ResponseEntity<ErrorResponseDTO> {
        val errors =
            configurationService.updateConfiguration(UpdateConfigurationRequest(requestDTO.config))
        if (errors.isNotEmpty()) {
            return ResponseEntity.badRequest().body(ErrorResponseDTO(errors.joinToString()))
        }
        return ResponseEntity.ok().build()
    }

    @GetMapping("/api/configuration/validate")
    fun validateAllConfiguration(
        @RequestBody appConfigurationDTO: AppConfigurationDTO
    ): ResponseEntity<ErrorResponseDTO> {
        val errors = configurationService.validateAllConfiguration(AppConfiguration(appConfigurationDTO.config))
        if (errors.isNotEmpty()) {
            return ResponseEntity.badRequest().body(ErrorResponseDTO(errors.joinToString()))
        }
        return ResponseEntity.ok().build()
    }

    @GetMapping("/api/configuration/validate/{platform}")
    fun validateConfiguration(
        @PathVariable platform: Platform,
        @RequestBody appConfigurationDTO: AppConfigurationDTO
    ): ResponseEntity<ErrorResponseDTO> {
        val appConfig = AppConfiguration(appConfigurationDTO.config)
        val error = configurationService.validateConfiguration(platform, appConfig)
        if (error != null) {
            return ResponseEntity.badRequest().body(ErrorResponseDTO(error))
        }
        return ResponseEntity.ok().build()
    }
}

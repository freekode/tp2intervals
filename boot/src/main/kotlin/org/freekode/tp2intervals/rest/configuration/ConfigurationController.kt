package org.freekode.tp2intervals.rest.configuration

import org.freekode.tp2intervals.app.confguration.ConfigurationService
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.config.UpdateConfigurationRequest
import org.freekode.tp2intervals.domain.workout.structure.StepModifier
import org.freekode.tp2intervals.rest.ErrorResponseDTO
import org.springframework.http.CacheControl
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
    private val publicSettings = setOf(
        "intervals.athlete-id", "intervals.power-range", "intervals.hr-range", "intervals.pace-range",
        "training-peaks.copy-plan-days-shift", "trainer-road.remove-html-tags", "general.debug-mode",
        "generic.beta-features-enabled",
    )
    private val credentialKeys = setOf("intervals.api-key", "training-peaks.auth-cookie", "trainer-road.auth-cookie")

    @GetMapping("/api/configuration")
    fun getConfigurations(): ResponseEntity<AppConfigurationDTO> {
        val config = configurationService.getConfigurations().configMap
        val response = AppConfigurationDTO(
            config.filterKeys { it in publicSettings },
            credentialKeys.associateWith { !config[it].isNullOrBlank() },
        )
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(response)
    }

    @PutMapping("/api/configuration")
    fun updateConfiguration(@RequestBody requestDTO: UpdateConfigurationRequestDTO): ResponseEntity<ErrorResponseDTO> {
        val errors = configurationService.updateConfiguration(UpdateConfigurationRequest(requestDTO.config))
        if (errors.isNotEmpty()) {
            return ResponseEntity.badRequest().body(ErrorResponseDTO("Configuration could not be saved. Check the account settings and credentials."))
        }
        return ResponseEntity.ok().build()
    }

    @GetMapping("/api/configuration/intervals-step-modifiers")
    fun getIntervalsStepModifiers(): List<StepModifier> {
        return StepModifier.entries
    }

    @GetMapping("/api/configuration/platform")
    fun getAllPlatformInfo() =
        configurationService.platformInfo()

    @GetMapping("/api/configuration/{platform}")
    fun getConfigurations(@PathVariable platform: Platform) =
        configurationService.platformInfo(platform)
}

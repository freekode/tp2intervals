package org.freekode.tp2intervals.app.confguration

import org.freekode.tp2intervals.domain.config.LogLevelService
import org.freekode.tp2intervals.domain.config.UpdateConfigurationRequest
import org.freekode.tp2intervals.infrastructure.configuration.AppConfigurationRepositoryImpl
import org.freekode.tp2intervals.infrastructure.configuration.ConfigurationCrudRepository
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsAthleteApiClient
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsConfigurationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.cache.CacheManager

// TODO fix tests
class ConfigurationServiceTest {
    private val appConfigurationRepository = AppConfigurationRepositoryImpl(mock(ConfigurationCrudRepository::class.java))

    private val configurationService = ConfigurationService(
        listOf(
            IntervalsConfigurationRepository(
                appConfigurationRepository,
                mock(IntervalsAthleteApiClient::class.java),
                mock(CacheManager::class.java)
            )
        ),
        listOf(),
        appConfigurationRepository,
        mock(LogLevelService::class.java)
    )

    @Test
    @Disabled
    fun `should set and update required configuration`() {
        // given
        val apiKey = "my-api"
        val athleteId = "i222222"
        val request = UpdateConfigurationRequest(
            mapOf(
                "intervals.api-key" to apiKey,
                "intervals.athlete-id" to athleteId,
                "my-test" to "test"
            )
        )

        val apiKey2 = "my-api2"
        val request2 = UpdateConfigurationRequest(mapOf("intervals.api-key" to apiKey2))

        // when
        val errors = configurationService.updateConfiguration(request)
        val updatedConfig = configurationService.getConfigurations()

        val errors2 = configurationService.updateConfiguration(request2)
        val updatedConfig2 = configurationService.getConfigurations()

        // then
        assertTrue(errors.isEmpty())
        assertTrue(errors2.isEmpty())

        assertEquals(updatedConfig.get("intervals.api-key"), apiKey)
        assertEquals(updatedConfig.get("intervals.athlete-id"), athleteId)
        assertEquals(updatedConfig2.get("intervals.api-key"), apiKey2)
        assertEquals(updatedConfig2.get("intervals.athlete-id"), athleteId)
        assertEquals(updatedConfig2.find("my-test"), null)
    }

    @Test
    @Disabled
    fun `should throw exception when remove required configuration`() {
        // given
        val athleteId = "-1"
        val request = UpdateConfigurationRequest(mapOf("intervals.athlete-id" to athleteId))
        val request2 = UpdateConfigurationRequest(mapOf("intervals.athlete-id" to ""))

        // when
        val errors = configurationService.updateConfiguration(request)
        val errors2 = configurationService.updateConfiguration(request2)
        val updatedConfig2 = configurationService.getConfigurations()

        // then
        assertTrue(errors.isNotEmpty())
        assertTrue(errors2.isNotEmpty())
        assertNotEquals(updatedConfig2.get("intervals.athlete-id"), "")
    }

    @Test
    @Disabled
    fun `should set and remove optional configuration`() {
        // given
        val authCookie = "my-tp-auth-cookie"
        val request = UpdateConfigurationRequest(mapOf("training-peaks.auth-cookie" to authCookie))
        val request2 = UpdateConfigurationRequest(mapOf("training-peaks.auth-cookie" to ""))
        val request3 = UpdateConfigurationRequest(mapOf("training-peaks.auth-cookie" to "-1"))

        // when
        val errors = configurationService.updateConfiguration(request)
        val updatedConfig = configurationService.getConfigurations()

        val errors2 = configurationService.updateConfiguration(request2)
        val updatedConfig2 = configurationService.getConfigurations()

        val errors3 = configurationService.updateConfiguration(request3)
        val updatedConfig3 = configurationService.getConfigurations()

        // then
        assertTrue(errors.isEmpty())
        assertTrue(errors2.isNotEmpty())
        assertTrue(errors3.isEmpty())
        assertEquals(updatedConfig.get("training-peaks.auth-cookie"), authCookie)
        assertEquals(updatedConfig2.get("training-peaks.auth-cookie"), authCookie)
        assertEquals(updatedConfig3.find("training-peaks.auth-cookie"), null)
    }

}

package org.freekode.tp2intervals.domain.config

import org.freekode.tp2intervals.domain.Platform
import org.springframework.stereotype.Repository

@Repository
class GenericPlatformConfigurationRepository(
    private val appConfigurationRepository: AppConfigurationRepository,

) : PlatformConfigurationRepository {
    override fun platform() = Platform.GENERIC

    override fun updateConfig(request: UpdateConfigurationRequest) {
        appConfigurationRepository.updateConfig(UpdateConfigurationRequest(request.getByPrefix("generic")))
    }
}

package org.freekode.tp2intervals.infrastructure.configuration

import org.freekode.tp2intervals.domain.config.AppConfiguration
import org.freekode.tp2intervals.domain.config.AppConfigurationRepository
import org.freekode.tp2intervals.domain.config.UpdateConfigurationRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class AppConfigurationRepositoryImpl(
    private val configurationCrudRepository: ConfigurationCrudRepository,
) : AppConfigurationRepository {

    override fun getConfiguration(key: String): String? {
        return configurationCrudRepository.findByIdOrNull(key)?.value
    }

    override fun getConfigurations(): AppConfiguration {
        return toDomain(configurationCrudRepository.findAll())
    }

    override fun getConfigurationByPrefix(prefix: String): AppConfiguration {
        return toDomain(configurationCrudRepository.findByKeyLike("$prefix%"))
    }

    override fun updateConfig(request: UpdateConfigurationRequest) {
        request.configMap.forEach { (key, value) ->
            if (value == "-1") {
                configurationCrudRepository.deleteById(key)
            } else {
                configurationCrudRepository.save(AppConfigurationEntryEntity(key, value))
            }
        }
    }

    private fun toDomain(entities: Iterable<AppConfigurationEntryEntity>): AppConfiguration {
        val configMap = entities.associateBy { it.key!! }.mapValues { it.value.value!! }
        return AppConfiguration(configMap)
    }
}

package org.freekode.tp2intervals.infrastructure.configuration

import org.freekode.tp2intervals.domain.config.AppConfiguration
import org.freekode.tp2intervals.domain.config.AppConfigurationRepository
import org.freekode.tp2intervals.domain.config.UpdateConfigurationRequest
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@CacheConfig(cacheNames = ["appConfiguration"])
@Repository
class AppConfigurationRepositoryImpl(
    private val dBConfigurationRepository: DBConfigurationRepository,
) : AppConfigurationRepository {

    @Deprecated("just dont need it")
    @Cacheable(key = "#key")
    override fun getConfiguration(key: String): AppConfiguration? {
        return dBConfigurationRepository.findByIdOrNull(key)?.let { toDomain(it) }
    }

    override fun getConfigurations(): AppConfiguration {
        return toDomain(dBConfigurationRepository.findAll())
    }

    override fun getConfigurationByPrefix(prefix: String): AppConfiguration {
        return toDomain(dBConfigurationRepository.findByKeyLike("$prefix%"))
    }

    override fun updateConfig(request: UpdateConfigurationRequest) {
        request.configMap.forEach { (key, value) ->
            dBConfigurationRepository.save(AppConfigurationEntryEntity(key, value))
        }
    }

    private fun toDomain(entities: Iterable<AppConfigurationEntryEntity>): AppConfiguration {
        val configMap = entities.associateBy { it.key!! }.mapValues { it.value.value!! }
        return AppConfiguration(configMap)
    }

    private fun toDomain(entity: AppConfigurationEntryEntity): AppConfiguration {
        return AppConfiguration(mapOf(Pair(entity.key!!, entity.value!!)))
    }
}

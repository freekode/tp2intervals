package org.freekode.tp2intervals.infrastructure.configuration

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ConfigurationCrudRepository : CrudRepository<AppConfigurationEntryEntity, String> {

    fun findByKeyLike(prefix: String): List<AppConfigurationEntryEntity>
}

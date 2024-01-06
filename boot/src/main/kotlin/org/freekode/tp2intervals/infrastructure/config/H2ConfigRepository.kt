package org.freekode.tp2intervals.infrastructure.config

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface H2ConfigRepository : CrudRepository<AppConfigEntity, Int>

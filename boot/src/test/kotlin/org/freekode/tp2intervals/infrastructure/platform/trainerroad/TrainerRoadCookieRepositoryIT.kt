package org.freekode.tp2intervals.infrastructure.platform.trainerroad

import config.SpringITConfig
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.cookie.TRCookieRepository
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class TrainerRoadCookieRepositoryIT : SpringITConfig() {
    @Autowired
    lateinit var trCookieRepository: TRCookieRepository

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            Thread.sleep(1000)
        }
    }

    @Test
    @Disabled
    fun `let see`() {
        trCookieRepository.getCookies()
    }
}

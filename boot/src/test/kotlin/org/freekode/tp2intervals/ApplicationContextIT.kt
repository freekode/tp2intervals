package org.freekode.tp2intervals

import config.SpringITConfig
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

class ApplicationContextIT : SpringITConfig() {
    @Autowired
    lateinit var context: ApplicationContext

    @Test
    fun `should start context`() {
        Assertions.assertNotNull(context)
    }
}

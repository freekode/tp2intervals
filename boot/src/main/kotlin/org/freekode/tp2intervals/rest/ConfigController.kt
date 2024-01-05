package org.freekode.tp2intervals.rest

import org.freekode.tp2intervals.app.ConfigService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ConfigController(
    private val configService: ConfigService
) {

    @GetMapping("test-connections")
    fun testConnections() {
        configService.testConnections()
    }
}

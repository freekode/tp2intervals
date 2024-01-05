package org.freekode.tp2intervals.app

import org.springframework.stereotype.Service

@Service
class ConfigService(
    private val connectionTesters: List<ConnectionTester>,
) {

    fun testConnections() {
        connectionTesters.forEach { it.test() }
    }
}

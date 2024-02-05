package org.freekode.tp2intervals.domain.config

class AppConfiguration(
    val configMap: Map<String, String>,
) {
    fun get(key: String): String = configMap[key]!!
}

package org.freekode.tp2intervals.infrastructure.platform.wahoosystm

import org.springframework.graphql.client.HttpGraphQlClient
import org.springframework.web.reactive.function.client.WebClient

class WahooSystmGraphQLClient {
    private val graphQLClient = HttpGraphQlClient.builder(
        WebClient.builder()
            .baseUrl("\${wahoo-systm.api-url}")
            .build()
    ).build()

    fun meme() {
        graphQLClient.documentName("login")
            .variable("username", "level.is03@gmail.com")
            .variable("password", "pZyU10ss79^Zm8y5")
    }
}

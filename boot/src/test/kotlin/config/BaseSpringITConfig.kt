package config

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource


@SpringBootTest
@ActiveProfiles("it", "dev")
abstract class BaseSpringITConfig {

    companion object {
        @JvmStatic
        @RegisterExtension
        protected var wireMockRule: WireMockExtension = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().dynamicPort())
            .build()

        @JvmStatic
        @DynamicPropertySource
        fun dynamicProperties(registry: DynamicPropertyRegistry) {
            registry.add("app.trainer-road.api-url") { "http://localhost:${wireMockRule.port}/trainer-road" }
        }

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            WireMock.configureFor("localhost", wireMockRule.port)
            Thread.sleep(1000)
        }
    }
}

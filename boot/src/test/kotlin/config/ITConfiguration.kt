package config

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class ITConfiguration {
    @Bean(initMethod = "start", destroyMethod = "stop")
    fun trainingPeaksMockServer(): WireMockServer {
        return WireMockServer(
            WireMockConfiguration.options()
                .port(4567)
        )
    }
}

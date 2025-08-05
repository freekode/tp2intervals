package config

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.token.TrainingPeaksUserTokenDTO
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.user.TrainingPeaksUserDTO
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource


@SpringBootTest
abstract class BaseSpringITConfig {

    @Autowired
    lateinit var objectMapper: ObjectMapper

    companion object {
//        @JvmStatic
//        @RegisterExtension
//        protected var wireMockServer = WireMockExtension.newInstance()
//            .options(WireMockConfiguration.wireMockConfig().port(34567))
//            .build()

        @JvmStatic
        @DynamicPropertySource
        fun dynamicProperties(registry: DynamicPropertyRegistry) {
            registry.add("app.trainer-road.api-url") { "http://localhost:${34567}/trainer-road" }
        }

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            Thread.sleep(1000) // wait for default properties to save
        }
    }

    fun tpTokenStub() {
        val response = TrainingPeaksUserTokenDTO("test-token")
        stubFor(
            get("/training-peaks/users/v3/token")
                .willReturn(okJson(objectMapper.writeValueAsString(response)))
        )
    }

    fun tpUserStub() {
        val response = TrainingPeaksUserDTO(
            userId = "user-id",
            accountStatus = TrainingPeaksUserDTO.TPUserAccountStatusDTO(isAthlete = true, isPremium = false)
        )
        stubFor(
            get("/training-peaks/users/v3/user")
                .willReturn(okJson(objectMapper.writeValueAsString(response)))
        )
    }

}

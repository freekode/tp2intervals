package config

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles


@SpringBootTest
@ActiveProfiles("it")
@Import(ITConfiguration::class)
abstract class SpringITConfig {
}

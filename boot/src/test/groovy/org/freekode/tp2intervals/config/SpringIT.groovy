package org.freekode.tp2intervals.config

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

@SpringBootTest
@ActiveProfiles("it")
@Import(ITestConfiguration.class)
abstract class SpringIT extends Specification {
}

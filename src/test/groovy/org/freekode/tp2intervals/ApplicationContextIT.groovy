package org.freekode.tp2intervals

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

@SpringBootTest
class ApplicationContextIT extends Specification {
    @Autowired
    ApplicationContext context

    def "should start context"() {
        expect:
        context != null
    }
}

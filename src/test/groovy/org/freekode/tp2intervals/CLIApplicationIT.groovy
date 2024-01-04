package org.freekode.tp2intervals

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

@SpringBootTest
@ActiveProfiles("dev")
class CLIApplicationIT extends Specification {
    @Autowired
    CLIApplication cliApplication

    def "should start context"() {
        expect:
        cliApplication.run("my-test", "my-test", "my-test", "my-test")
    }
}

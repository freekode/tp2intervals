package org.freekode.tp2intervals.infrastructure.thirdparty

import org.freekode.tp2intervals.app.ConnectionTester
import org.freekode.tp2intervals.infrastructure.thirdparty.token.ThirdPartyApiTokenRepository
import org.springframework.stereotype.Component

@Component
class ThirdPartyConnectionTester(
    private val thirdPartyApiTokenRepository: ThirdPartyApiTokenRepository,
) : ConnectionTester {
    override fun test() {
        thirdPartyApiTokenRepository.getToken()
    }
}

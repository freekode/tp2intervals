package org.freekode.tp2intervals.app.confguration


import org.freekode.tp2intervals.config.SpringIT
import org.freekode.tp2intervals.domain.config.UpdateConfigurationRequest
import org.springframework.beans.factory.annotation.Autowired

class ConfigurationServiceTest extends SpringIT {
    @Autowired
    ConfigurationService configurationService

    def "should set and update required configuration"() {
        given:
        def apiKey = "my-api"
        def athleteId = "my-athlete"
        def request = new UpdateConfigurationRequest([
                "intervals.api-key"   : apiKey,
                "intervals.athlete-id": athleteId,
                "my-test"             : "test"
        ])

        def apiKey2 = "my-api2"
        def request2 = new UpdateConfigurationRequest(["intervals.api-key": apiKey2])

        when:
        def errors = configurationService.updateConfiguration(request)
        def updatedConfig = configurationService.getConfigurations()

        def errors2 = configurationService.updateConfiguration(request2)
        def updatedConfig2 = configurationService.getConfigurations()

        then:
        errors.isEmpty()
        errors2.isEmpty()
        updatedConfig.get("intervals.api-key") == apiKey
        updatedConfig.get("intervals.athlete-id") == athleteId
        updatedConfig2.get("intervals.api-key") == apiKey2
        updatedConfig2.get("intervals.athlete-id") == athleteId
        updatedConfig2.find("my-test") == null
    }

    def "should throw exception when remove required configuration"() {
        given:
        def athleteId = "-1"
        def request = new UpdateConfigurationRequest(["intervals.athlete-id": athleteId])
        def request2 = new UpdateConfigurationRequest(["intervals.athlete-id": ""])

        when:
        def errors = configurationService.updateConfiguration(request)
        def errors2 = configurationService.updateConfiguration(request2)
        def updatedConfig2 = configurationService.getConfigurations()

        then:
        !errors.isEmpty()
        !errors2.isEmpty()
        updatedConfig2.get("intervals.athlete-id") != ""
    }

    def "should set and remove optional configuration"() {
        given:
        def authCookie = "my-cookie"
        def request = new UpdateConfigurationRequest(["training-peaks.auth-cookie": authCookie])
        def request2 = new UpdateConfigurationRequest(["training-peaks.auth-cookie": ""])
        def request3 = new UpdateConfigurationRequest(["training-peaks.auth-cookie": "-1"])

        when:
        def errors = configurationService.updateConfiguration(request)
        def updatedConfig = configurationService.getConfigurations()

        def errors2 = configurationService.updateConfiguration(request2)
        def updatedConfig2 = configurationService.getConfigurations()

        def errors3 = configurationService.updateConfiguration(request3)
        def updatedConfig3 = configurationService.getConfigurations()

        then:
        errors.isEmpty()
        !errors2.isEmpty()
        errors3.isEmpty()
        updatedConfig.get("training-peaks.auth-cookie") == authCookie
        updatedConfig2.get("training-peaks.auth-cookie") == authCookie
        updatedConfig3.find("training-peaks.auth-cookie") == null
    }

}

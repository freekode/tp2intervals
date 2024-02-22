package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure


import spock.lang.Specification

class ExternalDataConverterTest extends Specification {
    def "should parse external data"() {
        given:
        def string = "//////////\nintervalsId=111\ntrainingPeaksId=222\ntrainerRoadId=333"

        when:
        def data = new ExternalDataConverter().toExternalData(string)

        then:
        data != null
        data.intervalsId == "111"
        data.trainingPeaksId == "222"
        data.trainerRoadId == "333"
    }

    def "should throw exception with wrong variable"() {
        given:
        def string = "//////////\nmyFavVar=111"

        when:
        def data = new ExternalDataConverter().toExternalData(string)

        then:
        data == null
        thrown(IllegalArgumentException.class)
    }

    def "should not parse data"() {
        given:
        def string = "//////\nmyFavVar=111"

        when:
        def data = new ExternalDataConverter().toExternalData(string)

        then:
        data == null
    }

}

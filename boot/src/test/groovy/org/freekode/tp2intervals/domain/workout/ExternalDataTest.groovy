package org.freekode.tp2intervals.domain.workout

import org.freekode.tp2intervals.domain.ExternalData
import spock.lang.Specification

class ExternalDataTest extends Specification {
    def "should parse external data"() {
        given:
        def string = "//////////\nintervalsId=111\ntrainingPeaksId=222\ntrainerRoadId=333"

        when:
        def data = new ExternalData(null, null, null)
        data = data.withSimpleString(string)

        then:
        data != null
        data.intervalsId == "111"
        data.trainingPeaksId == "222"
        data.trainerRoadId == "333"
    }

    def "should not parse data"() {
        given:
        def string = "//////////\nmyFavVar=111"
        def string1 = "//////\nmyFavVar=111"

        when:
        def data = new ExternalData(null, null, null)
        data = data.withSimpleString(string)
        def data1 = new ExternalData(null, null, null)
        data1 = data1.withSimpleString(string1)

        then:
        data.trainingPeaksId == null
        data.intervalsId == null
        data.trainerRoadId == null
        data1.trainingPeaksId == null
        data1.intervalsId == null
        data1.trainerRoadId == null
    }

}

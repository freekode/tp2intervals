package org.freekode.tp2intervals.domain

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ExternalDataTest {
    @Test
    fun `should parse external data`() {
        // given
        val string = "//////////\nintervalsId=111\ntrainingPeaksId=222\ntrainerRoadId=333"

        // when
        var data = ExternalData(null, null, null)
        data = data.fromSimpleString(string)

        // then
        Assertions.assertNotNull(data)
        Assertions.assertEquals("111", data.intervalsId)
        Assertions.assertEquals("222", data.trainingPeaksId)
        Assertions.assertEquals("333", data.trainerRoadId)
    }

    @Test
    fun `should not parse data`() {
        // given
        val string = "//////////\nmyFavVar=111"
        val string1 = "//////\nmyFavVar=111"

        // when
        var data = ExternalData(null, null, null)
        data = data.fromSimpleString(string)
        var data1 = ExternalData(null, null, null)
        data1 = data1.fromSimpleString(string1)

        // then
        Assertions.assertNull(data.trainingPeaksId)
        Assertions.assertNull(data.intervalsId)
        Assertions.assertNull(data.trainerRoadId)
        Assertions.assertNull(data1.trainingPeaksId)
        Assertions.assertNull(data1.intervalsId)
        Assertions.assertNull(data1.trainerRoadId)
    }
}

package org.freekode.tp2intervals.infrastructure.utils

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Base64

class Base64Test {

    @Test
    fun `should decode base64 string to byte array`() {
        val original = "Hello World".toByteArray()
        val encoded = Base64.getEncoder().encodeToString(original)

        val result = Base64.getDecoder().decode(encoded)

        assertArrayEquals(original, result)
    }

    @Test
    fun `should handle empty string`() {
        val encoded = ""

        val result = Base64.getDecoder().decode(encoded)

        assertArrayEquals(ByteArray(0), result)
    }

    @Test
    fun `should handle binary data`() {
        val original = byteArrayOf(0, 1, 2, 127, -128, -1)
        val encoded = Base64.getEncoder().encodeToString(original)

        val result = Base64.getDecoder().decode(encoded)

        assertArrayEquals(original, result)
    }

    @Test
    fun `should throw on invalid base64`() {
        val invalid = "not-valid-base64!!!"

        assertThrows<IllegalArgumentException> {
            Base64.getDecoder().decode(invalid)
        }
    }
}

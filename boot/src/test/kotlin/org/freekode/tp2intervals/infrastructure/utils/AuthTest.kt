package org.freekode.tp2intervals.infrastructure.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Base64

class AuthTest {

    @Test
    fun `should generate basic auth header`() {
        val apiKey = "my-secret-key"

        val result = Auth.getAuthorizationHeader(apiKey)

        val expectedCredentials = "API_KEY:my-secret-key"
        val expectedBase64 = Base64.getEncoder().encodeToString(expectedCredentials.toByteArray())
        assertEquals("Basic $expectedBase64", result)
    }

    @Test
    fun `should generate different headers for different keys`() {
        val header1 = Auth.getAuthorizationHeader("key1")
        val header2 = Auth.getAuthorizationHeader("key2")

        assert(header1 != header2)
    }

    @Test
    fun `should handle empty api key`() {
        val result = Auth.getAuthorizationHeader("")

        assert(result.startsWith("Basic "))
        assert(result.length > 6)
    }
}

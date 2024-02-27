package org.freekode.tp2intervals.infrastructure.utils

import java.util.Base64

class Auth {
    companion object {
        private const val LOGIN = "API_KEY"

        fun getAuthorizationHeader(apiKey: String): String {
            val authorization = Base64.getEncoder().encodeToString(("$LOGIN:$apiKey").toByteArray())
            return "Basic $authorization"
        }
    }
}

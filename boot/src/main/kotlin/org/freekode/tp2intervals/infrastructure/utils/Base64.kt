package org.freekode.tp2intervals.infrastructure.utils

import org.springframework.core.io.Resource
import java.util.Base64

class Base64 {
    companion object {
        fun encodeToString(resource: Resource): String {
            val byteArray = resource.contentAsByteArray
            return Base64.getEncoder().encodeToString(byteArray)
        }

        fun decodeToByteArray(base64: String): ByteArray {
            return Base64.getDecoder().decode(base64)
        }
    }
}

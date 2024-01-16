package org.freekode.tp2intervals.infrastructure

import org.springframework.core.io.Resource
import java.util.Base64

class Base64 {
    companion object {
        fun toString(resource: Resource): String {
            val byteArray = resource.contentAsByteArray
            return Base64.getEncoder().encodeToString(byteArray)
        }
    }
}

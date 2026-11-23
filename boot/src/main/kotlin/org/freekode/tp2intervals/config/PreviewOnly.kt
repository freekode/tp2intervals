package org.freekode.tp2intervals.config

import feign.RequestInterceptor
import feign.RequestTemplate
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.server.ResponseStatusException

@Component
class PreviewOnly(@Value("\${app.preview-only:false}") val enabled: Boolean) {
    fun requireWritable() {
        if (enabled) throw ResponseStatusException(HttpStatus.FORBIDDEN, "PREVIEW_ONLY")
    }
}

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class PreviewOnlyProviderGuard(private val mode: PreviewOnly) : RequestInterceptor {
    override fun apply(template: RequestTemplate) {
        if (template.method() !in setOf("GET", "HEAD")) mode.requireWritable()
    }
}

@Component
class PreviewOnlyHttpGuard(private val mode: PreviewOnly) : OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val path = request.requestURI.removePrefix(request.contextPath)
        val preview = request.method == "POST" && path == "/api/workout/trainingpeaks-sync/preview"
        if (mode.enabled && !preview && request.method !in setOf("GET", "HEAD", "OPTIONS")) {
            response.status = HttpStatus.FORBIDDEN.value()
            response.contentType = "application/json"
            response.writer.write("""{"message":"PREVIEW_ONLY"}""")
            return
        }
        chain.doFilter(request, response)
    }
}

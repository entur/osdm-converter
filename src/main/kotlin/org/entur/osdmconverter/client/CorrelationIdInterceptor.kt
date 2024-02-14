package org.entur.osdmconverter.client

import feign.RequestInterceptor
import feign.RequestTemplate
import jakarta.servlet.http.HttpServletRequest
import org.entur.logging.context.TraceFilter
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

class CorrelationIdInterceptor : RequestInterceptor {

  override fun apply(template: RequestTemplate) {
    template.header(
        TraceFilter.CORRELATION_ID_HTTP_HEADER, TraceFilter.getCorrelationId(getCurrentRequest()))
  }

  private fun getCurrentRequest(): HttpServletRequest {
    val requestAttributes =
        RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes
    return requestAttributes.request
  }
}

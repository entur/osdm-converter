package org.entur.osdmconverter.client

import feign.RequestInterceptor
import feign.RequestTemplate

class EtClientNameInterceptor : RequestInterceptor {

  override fun apply(template: RequestTemplate) {
    template.header(
        "ET-Client-Name",
        "entur-hermod",
    )
  }
}

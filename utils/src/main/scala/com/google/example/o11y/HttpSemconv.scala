package com.google.example.o11y

import io.opentelemetry.api.common.AttributeKey

object HttpSemconv:
  val httpRoute = AttributeKey.stringKey("http.route")
  val clientAddress = AttributeKey.stringKey("client.address")
  val clientPort = AttributeKey.stringKey("client.port")
  val serverAddress = AttributeKey.stringKey("server.address")
  val serverPort = AttributeKey.longKey("server.port")
  val urlPath = AttributeKey.stringKey("url.path")
  val urlQuery = AttributeKey.stringKey("url.query")
  val urlScheme = AttributeKey.stringKey("url.scheme")
  val httpStatusCode = AttributeKey.longKey("http.response.status_code")
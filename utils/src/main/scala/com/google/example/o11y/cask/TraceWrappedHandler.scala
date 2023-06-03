/*
 * Copyright 2023 Google
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.example.o11y.cask

import io.undertow.server.{HttpHandler, HttpServerExchange}
import com.google.example.o11y.HttpSemconv
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.{Span, SpanKind, StatusCode, Tracer}
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.TextMapGetter

import scala.jdk.CollectionConverters.IterableHasAsJava
import scala.util.Using

/**
 * Wraps an undertow HttpHandler providing OOTB tracing.
 */
class TraceWrappedHandler(underlying: HttpHandler, tracer: Tracer) extends HttpHandler:
  override def handleRequest(exchange: HttpServerExchange): Unit =
    Using.Manager { use =>
      use(GlobalOpenTelemetry.getPropagators.getTextMapPropagator.extract(Context.current(), exchange, UndertowTextMapGetter).makeCurrent())
      val spanBuilder =
        tracer.spanBuilder(spanName(exchange))
          .setSpanKind(SpanKind.SERVER)
          .setAttribute(HttpSemconv.clientAddress, exchange.getConnection.getPeerAddress.toString)
          .setAttribute(HttpSemconv.serverAddress, exchange.getHostName)
          .setAttribute(HttpSemconv.serverPort, exchange.getHostPort.toLong)
          .setAttribute(HttpSemconv.urlPath, exchange.getRequestPath)
          .setAttribute(HttpSemconv.urlQuery, exchange.getQueryString)
          .setAttribute(HttpSemconv.urlScheme, exchange.getRequestScheme)
      val span: Span = spanBuilder.startSpan()
      use(span.makeCurrent())
      try underlying.handleRequest(exchange)
      catch
        case e: Exception =>
          span.recordException(e)
          throw e
      finally
        try
          span.setAttribute(HttpSemconv.httpStatusCode, exchange.getResponseCode)
          // TODO - sset error appropriately
          // TODO - check response sizes
        finally span.end()
    }

  private def spanName(exchange: HttpServerExchange): String =
    s"${exchange.getRequestMethod.toString} ${exchange.getRequestURI}"

/** Interaction between CASK and OTEL propagation. */
object UndertowTextMapGetter extends TextMapGetter[HttpServerExchange]:
  override def keys(r: HttpServerExchange): java.lang.Iterable[String] =
    r.getRequestHeaders.getHeaderNames.stream().map(_.toString).collect(java.util.stream.Collectors.toList)
  override def get(r: HttpServerExchange, key: String): String =
    val values = r.getRequestHeaders.get(key)
    if values.isEmpty then null
    else values.getFirst
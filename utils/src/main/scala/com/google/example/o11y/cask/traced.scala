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

import _root_.cask.{RawDecorator, Request, Response}
import com.google.example.o11y.HttpSemconv
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.{Span, SpanKind, StatusCode}
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.TextMapGetter
import scala.jdk.CollectionConverters.IterableHasAsJava
import scala.util.Using
import sourcecode.Text.generate

/** Cask wrapper that does context propagation and tracing */
class traced extends RawDecorator:
  val tracer = GlobalOpenTelemetry.getTracer("cask")
  def wrapFunction(ctx: Request, delegate: Delegate) =
    Using.Manager { use =>
      use(GlobalOpenTelemetry.getPropagators.getTextMapPropagator.extract(Context.current(), ctx, CaskTextMapGetter).makeCurrent())
      val spanBuilder =
        tracer.spanBuilder(spanName(ctx))
          .setSpanKind(SpanKind.SERVER)
          .setAttribute(HttpSemconv.clientAddress, ctx.exchange.getConnection.getPeerAddress.toString)
          .setAttribute(HttpSemconv.serverAddress, ctx.exchange.getHostName)
          .setAttribute(HttpSemconv.serverPort, ctx.exchange.getHostPort.toLong)
          .setAttribute(HttpSemconv.urlPath, ctx.exchange.getRequestPath)
          .setAttribute(HttpSemconv.urlQuery, ctx.exchange.getQueryString)
          .setAttribute(HttpSemconv.urlScheme, ctx.exchange.getRequestScheme)
      val span: Span = spanBuilder.startSpan()
      use(span.makeCurrent())
      try
        import _root_.cask.router.Result.*
        delegate(Map()) match
          case s: Success[Response.Raw] =>
            span.setAttribute(HttpSemconv.httpStatusCode, s.value.statusCode)
            span.setStatus(StatusCode.OK)
            s
          case e: Error =>
            span.setAttribute(HttpSemconv.httpStatusCode, 500)
            span.setStatus(StatusCode.ERROR)
            e match
              case Error.Exception(e) => span.recordException(e)
              case _ => // Ignore
            e
      finally
        span.end()
    }.get


  private def spanName(ctx: Request): String =
    s"${ctx.exchange.getRequestMethod.toString} ${ctx.exchange.getRequestURI}"


/** Interaction between CASK and OTEL propagation. */
object CaskTextMapGetter extends TextMapGetter[Request]:
  override def keys(r: Request): java.lang.Iterable[String] = r.headers.keys.asJava
  override def get(r: Request, key: String): String =
    val optResult = for
      values <- r.headers.get(key)
      head <- values.headOption
    yield head
    optResult.orNull
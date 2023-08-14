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

import cask.main.Main
import io.opentelemetry.api.GlobalOpenTelemetry
import io.undertow.server.handlers.BlockingHandler

import scala.util.Using

/** Overrides default CASK startup to make sure OTEL is ready. */
class OtelMainRoutes extends cask.MainRoutes:
  // This call HAS to be done at class-load time (not during main() method), because
  // too many shenanigans happen in CASK and OTEL becomes active prior to main() being called.
  com.google.example.o11y.initializeOpenTelemetry()
  override val log = CaskToSlf4jLogger()
  private val tracer = GlobalOpenTelemetry.getTracer("cask")

  override def defaultHandler = new BlockingHandler(
    new TraceWrappedHandler(
      new Main.DefaultHandler(dispatchTrie, mainDecorators, debugMode, handleNotFound, handleMethodNotAllowed, handleEndpointError)(using log),
      tracer)
  )

  
  
  inline def time[A](name: String)(inline f: => A): A =
    val span = tracer.spanBuilder(name).startSpan()
    try Using(span.makeCurrent())(_ => f).get
    finally span.end()
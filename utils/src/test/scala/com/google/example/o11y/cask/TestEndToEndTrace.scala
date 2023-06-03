package com.google.example.o11y.cask

import cask.main.Main
import com.google.example.o11y.{CuiKeys, CuiSpanProcessor, HttpSemconv}
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.{SpanKind, Tracer}
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import io.opentelemetry.sdk.testing.assertj.AttributeAssertion
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.`export`.SimpleSpanProcessor
import io.undertow.server.handlers.BlockingHandler
import io.undertow.Undertow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.*

class TestTraceWrappedHandler:
  @Test
  def testTracedEndpoint(): Unit =
    // Setup test OTEL
    val spans = InMemorySpanExporter.create()
    withTracer(spans) { tracer =>
      // Setup cask server
      withServer(MyTestApp(tracer)) { url =>
        // Make requests against it.
        assertEquals("Hello, World!", requests.get(url).text())
      }
      // Verify we have traces
      assertEquals(1, spans.getFinishedSpanItems.size())
      assertThat(spans.getFinishedSpanItems.get(0))
        .hasName("GET /")
        .hasKind(SpanKind.SERVER)
        .hasEnded
        .hasAttribute(HttpSemconv.serverAddress, "localhost")
        .hasAttribute(HttpSemconv.serverPort, 8081)
        .hasAttribute(HttpSemconv.urlPath, "/")
        .hasAttribute(HttpSemconv.urlQuery, "")
        .hasAttribute(HttpSemconv.urlScheme, "http")
        .hasAttribute(HttpSemconv.httpStatusCode, 200)
      // TODO - validate existence of clientAddress
    }

  @Test
  def testCuiEndpoint(): Unit =
    // Setup test OTEL
    val spans = InMemorySpanExporter.create()
    withTracer(spans) { tracer =>
      // Setup cask server
      withServer(MyTestApp(tracer)) { url =>
        // Make requests against it.
        assertEquals("Hello, cui!", requests.get(s"${url}/cui").text())
      }
      // Verify we have traces
      assertEquals(1, spans.getFinishedSpanItems.size())
      assertThat(spans.getFinishedSpanItems.get(0))
        .hasName("GET /cui")
        .hasAttribute(CuiKeys.cuiKey, "testcui")
    }

  def withTracer[T](spans: InMemorySpanExporter)(f: Tracer => T): T =
    val provider = SdkTracerProvider.builder()
      .addSpanProcessor(CuiSpanProcessor)
      .addSpanProcessor(SimpleSpanProcessor.create(spans))
      .build()
    try f(provider.get("test"))
    finally provider.close()

  // Starts a CASK server and returns the URL for it.
  def withServer[T](example: cask.main.Main)(f: String => T): T =
    val server = Undertow.builder
      .addHttpListener(8081, "localhost")
      .setHandler(example.defaultHandler)
      .build
    server.start()
    val res =
      try f("http://localhost:8081")
      finally server.stop()
    res


class MyTestApp(tracer: Tracer) extends cask.MainRoutes:
  initialize()
  override def defaultHandler = new BlockingHandler(
    new TraceWrappedHandler(
      new Main.DefaultHandler(dispatchTrie, mainDecorators, debugMode, handleNotFound, handleMethodNotAllowed, handleEndpointError)(using log),
      tracer)
  )
  @cask.get("/")
  def index() = "Hello, World!"

  @cui("testcui")
  @cask.get("/cui")
  def cui() = "Hello, cui!"

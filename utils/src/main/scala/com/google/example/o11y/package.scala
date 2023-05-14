package com.google.example.o11y

import io.opentelemetry.api.baggage.Baggage
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.{Span, SpanKind, StatusCode}
import io.opentelemetry.api.{GlobalOpenTelemetry, OpenTelemetry}
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.{TextMapGetter, TextMapSetter}
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import io.opentelemetry.sdk.logs.{LogRecordProcessor, ReadWriteLogRecord}
import io.opentelemetry.sdk.trace.{ReadWriteSpan, ReadableSpan, SpanProcessor}
import org.slf4j.bridge.SLF4JBridgeHandler

import scala.collection.mutable
import scala.jdk.CollectionConverters.IterableHasAsJava
import scala.util.Using

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

object CuiKeys:
  val cuiKey = AttributeKey.stringKey("cui")

/** A log processor that appends CUI attributes from baggage to logs. */
object CuiLogRecordProcessor extends LogRecordProcessor:
  override def onEmit(context: Context, logRecord: ReadWriteLogRecord): Unit =
    Baggage.fromContext(context).getEntryValue(CuiKeys.cuiKey.getKey) match
      case null =>
      case value =>
        logRecord.setAttribute(CuiKeys.cuiKey, value)

object CuiSpanProcessor extends SpanProcessor:
  override def isStartRequired: Boolean = true
  override def isEndRequired: Boolean = false
  override def onEnd(span: ReadableSpan): Unit = ()
  override def onStart(parentContext: Context, span: ReadWriteSpan): Unit =
    Baggage.fromContext(parentContext).getEntryValue(CuiKeys.cuiKey.getKey) match
      case null =>
      case value => span.setAttribute(CuiKeys.cuiKey, value)

/** Helper method to initialize open telemetry for OTLP export. */
def initializeOpenTelemetry(): Unit =
  println("Starting OpenTelemetry!")
  AutoConfiguredOpenTelemetrySdk.builder()
    .addPropertiesSupplier(() => java.util.Map.of("otel.logs.exporter", "otlp"))
    .addLoggerProviderCustomizer((provider, config) => provider.addLogRecordProcessor(CuiLogRecordProcessor))
    .addTracerProviderCustomizer((provider, config) => provider.addSpanProcessor(CuiSpanProcessor))
    .setResultAsGlobal(true)
    .build()
  // Hijack java.util.logging
  SLF4JBridgeHandler.removeHandlersForRootLogger()
  SLF4JBridgeHandler.install()
package com.google.example.o11y

import io.opentelemetry.api.{GlobalOpenTelemetry, OpenTelemetry}
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import org.slf4j.bridge.SLF4JBridgeHandler

/** Helper method to initialize open telemetry for OTLP export. */
def initializeOpenTelemetry(): Unit =
  AutoConfiguredOpenTelemetrySdk.builder()
    // Force logs to be exported OTLP by default.
    .addPropertiesSupplier(() => java.util.Map.of("otel.logs.exporter", "otlp"))
    .addLoggerProviderCustomizer((provider, config) => provider.addLogRecordProcessor(CuiLogRecordProcessor))
    .addTracerProviderCustomizer((provider, config) => provider.addSpanProcessor(CuiSpanProcessor))
    .setResultAsGlobal(true)
    .build()
  // Hijack java.util.logging
  SLF4JBridgeHandler.removeHandlersForRootLogger()
  SLF4JBridgeHandler.install()
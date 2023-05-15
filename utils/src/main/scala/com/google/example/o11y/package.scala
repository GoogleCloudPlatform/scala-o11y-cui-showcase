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
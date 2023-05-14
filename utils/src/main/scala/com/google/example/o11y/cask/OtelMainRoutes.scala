package com.google.example.o11y.cask

/** Overrides default CASK startup to make sure OTEL is ready. */
class OtelMainRoutes extends cask.MainRoutes:
  // This call HAS to be done at class-load time (not during main() method), because
  // too many shenanigans happen in CASK and OTEL becomes active prior to main() being called.
  com.google.example.o11y.initializeOpenTelemetry()
  override val log = CaskToSlf4jLogger()

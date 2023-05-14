package com.google.example.o11y.requests

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.context.Context

// For use w/ requests library
def propagatedHeaders(): Iterable[(String, String)] =
  val result = collection.mutable.Map[String, String]()
  GlobalOpenTelemetry.getPropagators.getTextMapPropagator.inject(
    Context.current(),
    result,
    (c, k, v) => c.put(k,v))
  result
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
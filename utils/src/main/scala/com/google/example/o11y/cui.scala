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

import io.opentelemetry.api.baggage.Baggage
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.context.{Context,Scope}
import io.opentelemetry.sdk.logs.{LogRecordProcessor, ReadWriteLogRecord}
import io.opentelemetry.sdk.trace.{ReadWriteSpan, ReadableSpan, SpanProcessor}

object CuiKeys:
  val cuiKey = AttributeKey.stringKey("cui")

/** Helper to inject CUI into Baggage. */
def injectCui(cui: String): Baggage =
  Baggage.fromContext(Context.current()).toBuilder
    .put(CuiKeys.cuiKey.getKey, cui)
    .build()

/** Helper to inject CUI into the Bagage an immediately make it current context. */
def injectCuiNow(cui: String): Scope =
  injectCui(cui).makeCurrent()

/** A log processor that appends CUI attributes from baggage to logs. */
object CuiLogRecordProcessor extends LogRecordProcessor:
  override def onEmit(context: Context, logRecord: ReadWriteLogRecord): Unit =
    Baggage.fromContext(context).getEntryValue(CuiKeys.cuiKey.getKey) match
      case null =>
      case value =>
        logRecord.setAttribute(CuiKeys.cuiKey, value)

/** Span processor that appends CUI attributes from baggage to spans. */
object CuiSpanProcessor extends SpanProcessor:
  override def isStartRequired: Boolean = true
  override def isEndRequired: Boolean = false
  override def onEnd(span: ReadableSpan): Unit = ()
  override def onStart(parentContext: Context, span: ReadWriteSpan): Unit =
    Baggage.fromContext(parentContext).getEntryValue(CuiKeys.cuiKey.getKey) match
      case null =>
      case value => span.setAttribute(CuiKeys.cuiKey, value)
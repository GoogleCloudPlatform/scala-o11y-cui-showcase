package com.google.example.o11y.cask

import com.google.example.o11y.cask
import io.opentelemetry.api.baggage.Baggage
import io.opentelemetry.context.Context

/** Cask wrapper that adds CUI to baggage for remaining processing */
class cui(val id: String) extends _root_.cask.RawDecorator:
  def wrapFunction(ctx: _root_.cask.Request, delegate: Delegate) =
    // Insert this CUI into baggage.
    val scope = Baggage.fromContext(Context.current()).toBuilder
      .put("cui", id)
      .build().makeCurrent()
    try delegate(Map())
    finally scope.close()
